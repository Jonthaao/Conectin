<template>
    <div class="how-it-works-container">
        <div class="how-it-works-box">
            <h1>Como Funciona o Conectin</h1>
            <p class="intro-text">
                O Conectin é a plataforma ideal para conectar clientes a trabalhadores informais de forma simples e
                segura. Veja como funciona:
            </p>

            <div class="steps-container">
                <div class="step-card" v-for="(step, index) in steps" :key="index">
                    <div class="step-number">{{ step.number }}</div>
                    <h3>{{ step.title }}</h3>
                    <p>{{ step.description }}</p>
                </div>
            </div>

            <div class="benefits-section">
                <h2>Benefícios</h2>
                <div class="benefits-grid">
                    <div class="benefit-card" v-for="(benefit, index) in benefits" :key="index">
                        <i class="benefit-icon">{{ benefit.icon }}</i>
                        <h3>{{ benefit.title }}</h3>
                        <p>{{ benefit.description }}</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'ComoFunciona',
    data() {
        return {
            steps: [
                {
                    number: '1',
                    title: 'Escolha a Categoria e Cidade',
                    description:
                        'Selecione a categoria do serviço (ex.: Pedreiro, Encanador) e informe sua cidade para encontrar prestadores próximos.',
                },
                {
                    number: '2',
                    title: 'Escolha um Prestador',
                    description:
                        'Veja o ranking dos mais bem avaliados e escolha o prestador com base em avaliações e portfólio.',
                },
                {
                    number: '3',
                    title: 'Contratação e Notificação',
                    description:
                        'Envie uma solicitação. O prestador será notificado e decidirá se aceita. Após a aceitação, troque contatos via WhatsApp.',
                },
                {
                    number: '4',
                    title: 'Avaliação Após o Serviço',
                    description:
                        'Após o serviço, ambos (cliente e prestador) podem avaliar a experiência, com comentários e fotos para referência futura.',
                },
            ],
            benefits: [
                {
                    icon: '👤',
                    title: 'Para Clientes',
                    description: 'Encontre profissionais qualificados e avalie suas experiências.',
                },
                {
                    icon: '🛠️',
                    title: 'Para Prestadores',
                    description: 'Aumente sua visibilidade com avaliações e construa um portfólio.',
                },
                {
                    icon: '🛡️',
                    title: 'Segurança',
                    description: 'Processo transparente com avaliações mútuas.',
                },
            ],
        };
    },
};
</script>

<style scoped>
.how-it-works-container {
    display: flex;
    justify-content: center;
    align-items: flex-start;
    min-height: calc(100vh - 80px);
    padding: 40px 20px;
    font-family: 'Roboto', 'Arial', sans-serif;
}

.how-it-works-box {
    background-color: rgba(255, 255, 255, 0.95);
    padding: 40px;
    border-radius: 15px;
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
    width: 100%;
    max-width: 1000px;
    text-align: left;
    border: none;
    position: relative;
    overflow: hidden;
}

.how-it-works-box::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 5px;
    background: #F4B400; /* Borda amarela no topo */
}

h1 {
    font-size: 2.5rem;
    color: #257BB8; /* Cor azul da logo */
    text-align: center;
    margin-bottom: 30px;
    position: relative;
    padding-bottom: 15px;
    font-weight: 700;
}

h1::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 80px;
    height: 3px;
    background-color: #F4B400; /* Linha amarela abaixo do título */
}

.intro-text {
    font-size: 1.2rem;
    color: #444;
    margin-bottom: 40px;
    text-align: center;
    max-width: 800px;
    margin-left: auto;
    margin-right: auto;
    line-height: 1.6;
}

.steps-container {
    display: flex;
    flex-wrap: wrap;
    gap: 25px;
    margin-bottom: 50px;
    justify-content: center;
}

.step-card {
    flex: 1;
    min-width: 220px;
    background-color: #FFFFFF;
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
    border: none;
    transition: transform 0.3s, box-shadow 0.3s;
    position: relative;
    overflow: hidden;
}

.step-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 5px;
    height: 100%;
    background-color: #257BB8; /* Borda lateral azul */
}

.step-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 20px rgba(0, 0, 0, 0.12);
}

.step-number {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.4rem;
    color: #FFFFFF;
    font-weight: bold;
    margin-bottom: 15px;
    width: 40px;
    height: 40px;
    background-color: #F4B400; /* Fundo amarelo para o número */
    border-radius: 50%;
}

.step-card h3 {
    font-size: 1.3rem;
    color: #257BB8; /* Título azul */
    margin-bottom: 12px;
    font-weight: 600;
}

.step-card p {
    font-size: 1rem;
    color: #555;
    line-height: 1.5;
}

.benefits-section {
    margin-top: 50px;
    background-color: rgba(37, 123, 184, 0.05); /* Fundo azul claro */
    padding: 30px;
    border-radius: 12px;
}

.benefits-section h2 {
    font-size: 1.8rem;
    color: #257BB8; /* Título azul */
    text-align: center;
    margin-bottom: 30px;
    position: relative;
    padding-bottom: 12px;
    font-weight: 600;
}

.benefits-section h2::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 3px;
    background-color: #F4B400; /* Linha amarela abaixo do título */
}

.benefits-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 25px;
}

.benefit-card {
    background-color: #FFFFFF;
    padding: 25px;
    border-radius: 12px;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    text-align: center;
    transition: transform 0.3s, box-shadow 0.3s;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.benefit-card:hover {
    transform: translateY(-6px);
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.benefit-icon {
    font-size: 2rem;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60px;
    height: 60px;
    background-color: #F4B400; /* Fundo azul para o ícone */
    color: #FFFFFF;
    border-radius: 50%;
    font-style: normal;
    position: relative;
    box-shadow: 0 4px 8px rgba(37, 123, 184, 0.25);
}

.benefit-card h3 {
    font-size: 1.3rem;
    color: #257BB8; /* Título azul */
    margin-bottom: 12px;
    font-weight: 600;
}

.benefit-card p {
    font-size: 1rem;
    color: #555;
    line-height: 1.6;
}

/* Responsividade */
@media (max-width: 768px) {
    .how-it-works-box {
        padding: 30px 20px;
    }
    
    .steps-container {
        flex-direction: column;
    }
    
    .step-card {
        min-width: 100%;
    }
    
    h1 {
        font-size: 2rem;
    }
    
    .intro-text {
        font-size: 1.1rem;
    }
}
</style>