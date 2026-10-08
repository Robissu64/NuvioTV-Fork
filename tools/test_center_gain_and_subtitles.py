"""Run focused JVM tests without an Android SDK, using an existing Kotlin/JUnit runtime."""
from pathlib import Path
import argparse
import os
import subprocess
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument('--runtime', type=Path, required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
runtime = args.runtime.resolve()
java = next(runtime.glob('jdk-*/bin/java.exe'))
classpath = os.pathsep.join(str(p) for p in runtime.glob('*.jar'))
classes = root / 'app/build/focused-jvm-tests'
classes.mkdir(parents=True, exist_ok=True)
media3 = classes.parent / 'focused-media3-common.jar'
with zipfile.ZipFile(root / 'app/libs/lib-common-release.aar') as aar:
    media3.write_bytes(aar.read('classes.jar'))
classpath += os.pathsep + str(media3)
units = [
    ('core/player', 'CenterChannelGainPolicy'),
    ('core/player', 'AudioPassthroughPolicy'),
    ('core/player', 'DeniedTranscodePlanner'),
    ('core/player', 'LocalSubtitleFiles'),
    ('core/player', 'SubtitleCharsetDetector'),
    ('core/player', 'SubtitleVisibility'),
    ('core/server', 'LocalSubtitleTransferServer'),
]
sources, tests = [], []
for folder, name in units:
    sources.extend([
        root / f'app/src/main/java/com/nuvio/tv/{folder}/{name}.kt',
        root / f'app/src/test/java/com/nuvio/tv/{folder}/{name}Test.kt',
    ])
    tests.append('com.nuvio.tv.' + folder.replace('/', '.') + '.' + name + 'Test')
subprocess.run([str(java), '-cp', classpath, 'org.jetbrains.kotlin.cli.jvm.K2JVMCompiler',
                '-no-stdlib', '-no-reflect', '-classpath', classpath, '-jvm-target', '11',
                '-d', str(classes), *map(str, sources)], check=True)
subprocess.run([str(java), '-cp', str(classes) + os.pathsep + classpath,
                'org.junit.runner.JUnitCore', *tests], check=True)
