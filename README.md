# RockShowExplorer 🤘🎸

## Descrição do Projeto
O **RockShowExplorer** é uma aplicação Android desenvolvida com **Kotlin** e **Jetpack Compose** que serve como um arquivo digital para fãs de Rock e Heavy Metal. O objetivo principal é catalogar e arquivar presenças em concertos, permitindo registar informações detalhadas e digitalizar as recordações físicas (bilhetes e setlists). 

A aplicação comunica em tempo real com a base de dados **Firebase Firestore**, garantindo que todas as informações são guardadas e atualizadas instantaneamente na nuvem.

## 🎥 Vídeo de Demonstração
No vídeo abaixo é demonstrado o funcionamento completo da aplicação, incluindo a sincronização em tempo real das operações CRUD com o Firebase Firestore:

👉 **[Clique aqui para assistir à demonstração da aplicação (Google Drive)](https://drive.google.com/file/d/18ukGuPYWY0mYr_LJ2mCWtj01gFRE5eHY/view?usp=sharing)**

## Funcionalidades Implementadas (CRUD Completo)
O projeto cumpre todos os requisitos de operações de banco de dados:
- **Create (Criar)**: Adição de novos concertos com nome da banda, data da turné (com validação de máscara), arena visitada e a digitalização/anexo de fotos do bilhete e do setlist.
- **Read (Ler)**: Listagem em tempo real de todos os concertos guardados, diretamente do Firestore, exibidos em cartões personalizados.
- **Update (Atualizar)**: Possibilidade de clicar em qualquer registo existente para editar e corrigir informações.
- **Delete (Apagar)**: Remoção de concertos do arquivo, com reflexo imediato na base de dados.

## Identidade Visual e UI/UX
- Interface construída integralmente com componentes **Material Design 3** no Jetpack Compose.
- Tema escuro (*Dark Theme*) implementado com uma paleta de cores personalizada (preto, cinzento escuro e vermelho sangue) para respeitar a estética exigida pelo tema de Heavy Metal.
- Sistema de autenticação (Login/Registo) com **Firebase Authentication** para proteção e validação de acesso.

## Tecnologias e Bibliotecas Utilizadas
- **Linguagem**: Kotlin
- **Interface Gráfica**: Jetpack Compose
- **Base de Dados**: Firebase Firestore (NoSQL em tempo real)
- **Autenticação**: Firebase Auth
- **Carregamento de Imagens**: Coil (para carregar URLs de imagens de forma assíncrona)
- **Arquitetura**: Baseada no padrão MVVM (Model-View-ViewModel) utilizando `ViewModel` e `StateFlow`.
