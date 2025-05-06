<template>
    <header class="navbar">
      <div class="navbar-container">
        <img src="../../public/images/logoConectin-removebg.png" alt="Conectin Logo" class="logo" />
  
        <!-- Menu esquerdo -->
        <nav class="nav-links">
          <router-link to="/">Home</router-link>
          <router-link to="/comofunciona">Como funciona</router-link>
          <router-link to="/ranking-prestadores">Prestadores</router-link>
        </nav>
  
        <!-- Menu direito (avatar/login) -->
        <div class="user-menu"> 
          <AvatarDropdown :default-avatar="defaultAvatar" />
        </div>
      </div>
    </header>
  </template>
  
  <script setup>
  import AvatarDropdown from './AvatarDropdown.vue';
  
  const defaultAvatar = 'https://www.gravatar.com/avatar/?d=mp';
  </script>
  
  <style scoped>



  .navbar {
    background: linear-gradient(135deg, #257BB8 0%, #1557B0 100%);
    padding: 15px 0;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    }
  
  .navbar-container {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
  }
  
  .logo {
    width: 100px;
    height: auto;
  }
  
  .nav-links {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  
  .nav-links a {
    font-size: 20px;
    font-weight: bold;
    color: #fff;
    text-decoration: none;
    transition: color 0.3s;
  }
  
  .nav-links a:hover,
  .nav-links a.router-link-exact-active {
    color: #f4b400;
  }
  
  .user-menu {
    position: relative;
  }
  </style>