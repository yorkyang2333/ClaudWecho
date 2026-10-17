// Generate deterministic reference fingerprints using the pinned upstream JS/WASM.
import { createHash } from 'node:crypto';
import { mkdir, writeFile } from 'node:fs/promises';
import vm from 'node:vm';

const base = 'https://raw.githubusercontent.com/NeteaseCloudMusicApiEnhanced/api-enhanced/2db6684453297ee86dec0492842f2320b08182d9/public/audio_match_demo/';
const expected = ['4926a6d69527a1afbc7f7d120b9c21947e677d80c484c8b65572fa7d4ce3b99f', '3776f3122d8a516d716ec00f55c24b919f03a2fb4bb009191007327170d33763'];
const scripts = await Promise.all(['afp.wasm.js', 'afp.js'].map(async (file, i) => {
  const response = await fetch(base + file);
  if (!response.ok) throw new Error(`Cannot download ${file}: ${response.status}`);
  const source = await response.text();
  if (createHash('sha256').update(source).digest('hex') !== expected[i]) throw new Error('Upstream checksum mismatch');
  return source;
}));
const context = vm.createContext({ console: { log() {}, info() {}, warn() {} }, atob, btoa, setTimeout, clearTimeout, setInterval, clearInterval, TextDecoder, TextEncoder });
scripts.forEach(source => vm.runInContext(source, context));
const fixtures = {
  silence: new Float32Array(24000),
  mixture: Float32Array.from({ length: 24000 }, (_, i) => ((i % 97) - 48) / 128 + ((i % 31) - 15) / 64),
};
const directory = new URL('../app/src/androidTest/assets/recognition/', import.meta.url);
if (process.argv.includes('--write')) await mkdir(directory, { recursive: true });
for (const [name, samples] of Object.entries(fixtures)) {
  context.samples = samples;
  const fingerprint = await vm.runInContext('GenerateFP(samples)', context);
  console.log(`${name}: ${fingerprint}`);
  if (process.argv.includes('--write')) {
    await writeFile(new URL(`${name}.f32`, directory), new Uint8Array(samples.buffer));
    await writeFile(new URL(`${name}.fp`, directory), Buffer.from(fingerprint, 'base64'));
  }
}
