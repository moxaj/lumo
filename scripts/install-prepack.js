const child_process = require('child_process');

// prettier-ignore
child_process.execSync(
  'git clone --depth=1 https://github.com/facebook/prepack.git && cd prepack && yarn install && cd ..',
  { stdio: [0, 1, 2] }
);
