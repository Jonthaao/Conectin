<template>
    <div>
      <h2>Perfil do Cliente</h2>
      <p>Nome: {{ cliente.nome }}</p>
      <p>Email: {{ cliente.email }}</p>
      <!-- Outros dados do cliente -->
      <button @click="editarPerfil">Editar Perfil</button>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        cliente: {
          nome: 'João Silva', // Exemplo, substitua pela sua lógica de dados
          email: 'joao@email.com',
        },
      };
    },
    methods: {
      editarPerfil() {
        this.$router.push('/editar-perfil');
      },
    },
  };
  </script>