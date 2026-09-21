// SPDX-License-Identifier: GPL-3.0-only
#ifndef NUVIO_CENTER_GAIN_H
#define NUVIO_CENTER_GAIN_H

#include <cmath>

namespace nuvio_center {
// V1.1: linear FC gain, no peak shaping or global attenuation.
// Values above full scale are deliberately retained here; downstream clipping
// is possible. Invalid floating-point samples are silenced on FC only.

inline bool apply(float** planes, int channels, int centerIndex, int samples, int db) {
  if (!planes || centerIndex < 0 || centerIndex >= channels || samples <= 0 || db == 0)
    return false;
  float* center = planes[centerIndex];
  if (!center) return false;
  // V1 safety ceiling: JNI callers cannot request more than +4 dB.
  if (db < 0) return false;
  if (db > 4) db = 4;
  const float gain = std::pow(10.0f, db / 20.0f);
  for (int i = 0; i < samples; ++i) {
    const float boosted = center[i] * gain;
    center[i] = std::isfinite(boosted) ? boosted : 0.0f;
  }
  return true;
}
}  // namespace nuvio_center
#endif
