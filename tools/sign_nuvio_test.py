"""Create/reuse the private Nuvio Teste key and sign only its separate package.

Uses Python's standard library, JDK 17 and Android SDK Build Tools. Never copies
credentials into the repository or prints passwords. Back up the private folder
separately; both the keystore and password file are needed after moving machines.
"""

import argparse
import csv
import hashlib
import json
import os
from pathlib import Path
import re
import secrets
import subprocess
import tempfile

PACKAGE = "com.nuvio.tv.center.test"
LABEL = "Nuvio Teste"
ALIAS = "nuvio-teste"


def run(args, **kwargs):
    result = subprocess.run([str(x) for x in args], capture_output=True, **kwargs)
    if result.returncode:
        # Commands contain paths/file references, never an inline password.
        raise RuntimeError(result.stderr.decode(errors="replace"))
    return result.stdout


def private_paths(folder):
    folder = folder.expanduser().resolve()
    if any((parent / ".git").exists() for parent in (folder, *folder.parents)):
        raise ValueError("The signing folder must be outside Git checkouts.")
    return folder, folder / "nuvio-teste.p12", folder / "password.txt", folder / "key.json"


def certificate(jdk, key, password):
    der = run([jdk / "bin/keytool.exe", "-exportcert", "-alias", ALIAS,
               "-keystore", key, "-storepass:file", password])
    return hashlib.sha256(der).hexdigest()


def initialize(folder, jdk):
    folder, key, password, config = private_paths(folder)
    if key.exists() or password.exists() or config.exists():
        # Fail on partial state rather than silently replacing an installed key.
        metadata = json.loads(config.read_text(encoding="utf-8"))
        if certificate(jdk, key, password) != metadata["certificate_sha256"]:
            raise ValueError("Existing keystore does not match the pinned certificate.")
        print("Existing persistent key verified:", key)
        return
    if os.name != "nt":
        raise ValueError("Key initialization requires Windows ACL protection.")
    folder.mkdir(parents=True, exist_ok=True)
    user = next(csv.reader(run(["whoami", "/user", "/fo", "csv", "/nh"])
                           .decode().strip().splitlines()))[1]
    run(["icacls", folder, "/inheritance:r", "/grant:r",
         f"*{user}:(OI)(CI)F", "*S-1-5-18:(OI)(CI)F"])
    password.write_text(secrets.token_urlsafe(40) + "\n", encoding="ascii")
    run([jdk / "bin/keytool.exe", "-genkeypair", "-keystore", key,
         "-storetype", "PKCS12", "-alias", ALIAS, "-keyalg", "RSA",
         "-keysize", "3072", "-validity", "10950",
         "-storepass:file", password, "-keypass:file", password,
         "-dname", "CN=Nuvio Teste, OU=Private Test Build, O=Nuvio Fork, C=BR"])
    metadata = {"applicationId": PACKAGE, "alias": ALIAS,
                "certificate_sha256": certificate(jdk, key, password),
                "keystore": key.name, "password_file": password.name}
    config.write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
    print("Persistent key created:", key)
    print("Certificate SHA-256:", metadata["certificate_sha256"])


def apk_identity(build_tools, apk):
    badging = run([build_tools / "aapt2.exe", "dump", "badging", apk]).decode("utf-8")
    package = re.search(r"^package: name='([^']+)'", badging, re.M).group(1)
    label = re.search(r"^application-label:'([^']+)'", badging, re.M).group(1)
    if (package, label) != (PACKAGE, LABEL):
        raise ValueError(f"Refusing to sign another identity: {package} / {label}")
    return badging


def sign(folder, jdk, build_tools, source, output):
    folder, key, password, config = private_paths(folder)
    metadata = json.loads(config.read_text(encoding="utf-8"))
    expected = certificate(jdk, key, password)
    if metadata["applicationId"] != PACKAGE or expected != metadata["certificate_sha256"]:
        raise ValueError("Persistent signing key does not match the pinned certificate.")
    source, output = source.resolve(), output.resolve()
    apk_identity(build_tools, source)
    if output.exists() or source == output:
        raise ValueError("Use a new output path; existing APKs will not be overwritten.")
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="nuvio-test-sign-", dir=output.parent) as temp:
        aligned, signed = Path(temp) / "aligned.apk", Path(temp) / "signed.apk"
        run([build_tools / "zipalign.exe", "-P", "16", "-f", "4", source, aligned])
        signer = [jdk / "bin/java.exe", "-jar", build_tools / "lib/apksigner.jar"]
        run([*signer, "sign", "--ks", key, "--ks-key-alias", ALIAS,
             "--ks-pass", f"file:{password}", "--key-pass", f"file:{password}",
             "--v1-signing-enabled", "true", "--v2-signing-enabled", "true",
             "--v3-signing-enabled", "true", "--v4-signing-enabled", "false",
             "--out", signed, aligned])
        verification = run([*signer, "verify", "--verbose", "--print-certs", signed]).decode()
        fingerprints = re.findall(r"Signer #\d+ certificate SHA-256 digest: (\w+)", verification)
        if fingerprints != [expected]:
            raise ValueError("APK signing certificate mismatch.")
        run([build_tools / "zipalign.exe", "-c", "-P", "16", "4", signed])
        apk_identity(build_tools, signed)
        signed.rename(output)
    print("Signed and verified:", output)
    print("Certificate SHA-256:", expected)
    print("APK SHA-256:", hashlib.sha256(output.read_bytes()).hexdigest())
    output.with_suffix(".signature.txt").write_text(verification, encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--private-dir", type=Path,
                        default=Path.home() / ".nuvio-signing/nuvio-teste")
    parser.add_argument("--jdk", type=Path, required=True)
    commands = parser.add_subparsers(dest="command", required=True)
    commands.add_parser("init")
    signing = commands.add_parser("sign")
    signing.add_argument("--build-tools", type=Path, required=True)
    signing.add_argument("--input", type=Path, required=True)
    signing.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    if args.command == "init":
        initialize(args.private_dir, args.jdk)
    else:
        sign(args.private_dir, args.jdk, args.build_tools, args.input, args.output)


if __name__ == "__main__":
    main()
