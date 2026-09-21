// SPDX-License-Identifier: GPL-3.0-only
#ifndef NUVIO_CENTER_GAIN_H
#define NUVIO_CENTER_GAIN_H

#include <cmath>

namespace nuvio_center {
// Memoryless soft peak protection, NOT a transparent look-ahead limiter.
// Exact requested gain below 0.90; asymptotically bounded by 0.98 above it.
// Only FC changes. Other channels have no attenuation/headroom adjustment.
inline float protect(float sample) {
  if (!std::isfinite(sample)) return 0.0f;
  const float magnitude = std::fabs(sample);
  if (magnitude <= 0.90f) return sample;
  const float excess = magnitude - 0.90f;
  const float shaped = 0.90f + 0.08f * (excess / (0.08f + excess));
  return std::copysign(shaped, sample);
}

inline bool apply(float** planes, int channels, int centerIndex, int samples, int db) {
  if (!planes || centerIndex < 0 || centerIndex >= channels || samples <= 0 || db == 0)
    return false;
  float* center = planes[centerIndex];
  if (!center) return false;
  // V1 safety ceiling: JNI callers cannot request more than +4 dB.
  if (db < 0) return false;
  if (db > 4) db = 4;
  const float gain = std::pow(10.0f, db / 20.0f);
  for (int i = 0; i < samples; ++i) center[i] = protect(center[i] * gain);
  return true;
}
}  // namespace nuvio_center
#endif
