module.exports = {
    root: true,
    env: {
      browser: true,
      es2021: true,
      node: true,
      'vue/setup-compiler-macros': true,
    },
    extends: [
      'eslint:recommended',
      'plugin:vue/vue3-essential', // Para Vue 3
      'eslint:recommended',
    ],
    parserOptions: {
      parser: '@babel/eslint-parser', // Use o parser do Babel
      requireConfigFile: false, // Desativa a verificação do arquivo de configuração do Babel
    },
    rules: {
      'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
      'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    },
  };