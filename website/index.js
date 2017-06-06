import path from 'path';

export default (async function renderPage(requestPath) {
  const component = await import(
    /* webpackChunkName: "[request]" */
    `./src/${path.join(requestPath)}.js`,
  );

  return component.default;
});
