// SPDX-License-Identifier: GPL-3.0-only
#include "../ffmpeg-decoder-downmix/src/main/jni/center_gain.h"
#include <cassert>
#include <cstring>
#include <iostream>
#include <limits>

int main() {
  const float gain = std::pow(10.0f, 4.0f / 20.0f);
  // Test a center at every position: the algorithm must not assume index 2.
  for (int count : {3, 6, 8}) {
    for (int fc = 0; fc < count; ++fc) {
      float audio[8][8] = {};
      float original[8][8];
      float* planes[8];
      for (int c = 0; c < count; ++c) {
        planes[c] = audio[c];
        for (int s = 0; s < 8; ++s) audio[c][s] = (s - 4) * 0.05f;
      }
      std::memcpy(original, audio, sizeof(audio));
      assert(!nuvio_center::apply(planes, count, fc, 8, 0));
      assert(std::memcmp(audio, original, sizeof(audio)) == 0);
      assert(nuvio_center::apply(planes, count, fc, 8, 4));
      for (int c = 0; c < count; ++c) {
        for (int s = 0; s < 8; ++s) {
          if (c == fc) assert(std::fabs(audio[c][s] - original[c][s] * gain) < 1e-6f);
          else assert(audio[c][s] == original[c][s]);
        }
      }
    }
  }
  float edge[] = {0.0f, 0.8f, -0.8f, 1.0f, -1.0f, 50.0f,
      std::numeric_limits<float>::infinity(), std::numeric_limits<float>::quiet_NaN()};
  float* plane[] = {edge};
  assert(!nuvio_center::apply(nullptr, 6, 2, 8, 4));
  assert(!nuvio_center::apply(plane, 1, -1, 8, 4));
  assert(!nuvio_center::apply(plane, 1, 1, 8, 4));
  assert(!nuvio_center::apply(plane, 1, 0, 0, 4));
  assert(!nuvio_center::apply(plane, 1, 0, 8, -1));
  assert(nuvio_center::apply(plane, 1, 0, 8, 30)); // clamped to +4
  for (float value : edge) assert(std::isfinite(value) && std::fabs(value) <= 0.980001f);
  assert(edge[0] == 0.0f && edge[6] == 0.0f && edge[7] == 0.0f);
  assert(std::fabs(edge[1] + edge[2]) < 1e-6f);
  assert(std::fabs(edge[3] + edge[4]) < 1e-6f);
  // Continuous, odd, monotonic soft protection over an extensive amplitude sweep.
  float last = 0.0f;
  for (int i = 0; i < 100000; ++i) {
    float input = i * 0.0001f;
    float output = nuvio_center::protect(input);
    assert(output >= last && output <= 0.980001f);
    assert(std::fabs(output + nuvio_center::protect(-input)) < 1e-6f);
    last = output;
  }
  std::cout << "PASS: +4 dB; non-FC unchanged; bypass; channel positions; peak bounds; invalid input\n";
}
