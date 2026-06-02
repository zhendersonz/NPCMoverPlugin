# NPCMoverPlugin 🚶‍♂️

NPCMoverPlugin é um plugin standalone para Minecraft Java (Paper/Spigot) que permite criar NPCs dinâmicos com rotas de movimento personalizadas, pausas inteligentes e interatividade total.

## 🌟 Funcionalidades Principais

*   **Criação Simples**: Crie NPCs de qualquer tipo (Villager, Zombie, Illager, etc.) com nomes personalizados.
*   **Editor com Graveto**: Use um graveto (`/npcwand`) para marcar pontos de movimento e pausa de forma visual e intuitiva.
*   **Movimento Fluido**: Sistema de navegação otimizado (1 tick) para caminhadas suaves e naturais.
*   **Pausas Manuais e Olhar Fixo**: Defina pontos onde o NPC deve parar por X segundos e olhar para uma direção específica.
*   **Interatividade**: Vincule comandos aos NPCs para que eles executem ações ao serem clicados.
*   **Velocidade Customizada**: Ajuste a velocidade de cada NPC individualmente.
*   **Otimização de Performance**: NPCs entram em "modo de espera" se não houver jogadores por perto, economizando recursos do servidor.
*   **Segurança e Proteção**: NPCs são invulneráveis, imunes ao sol/fogo e protegidos contra plugins de limpeza (ClearLag).
*   **Persistência Total**: Salva automaticamente todos os NPCs e suas rotas em arquivos YAML.
*   **Gestão Facilitada**: Comandos de teletransporte (`tp`, `tphere`) e visualização de rota com partículas.

## 🛠️ Comandos

| Comando | Descrição |
| :--- | :--- |
| `/npcmove create <tipo> <nome...>` | Cria um novo NPC na sua posição. |
| `/npcmove path <nome...>` | Entra no modo de edição de caminho com o graveto. |
| `/npcmove pause <segundos>` | Define o tempo de pausa para os próximos pontos criados. |
| `/npcmove speed <nome...> <valor>` | Ajusta a velocidade do NPC (ex: 1.0 é padrão). |
| `/npcmove cmd <nome...> <comando>` | Define um comando ao clicar (use `{player}` para o nome do jogador). |
| `/npcmove show <nome...>` | Mostra o caminho do NPC com partículas por 10s. |
| `/npcmove tp <nome...>` | Teleporta você até o NPC. |
| `/npcmove tphere <nome...>` | Teleporta o NPC até você. |
| `/npcmove remove <nome...>` | Remove permanentemente o NPC. |
| `/npcmove list` | Lista todos os NPCs registrados. |
| `/npcwand` | Recebe o graveto de edição. |

## 🎮 Como Usar o Graveto

No modo de edição (`/npcmove path <nome>`):
*   **Clique Esquerdo**: Adiciona um ponto de **MOVIMENTO** (o NPC apenas passa por lá).
*   **Shift + Clique Direito**: Adiciona um ponto de **PAUSA** (o NPC para e olha para onde você está virado).
*   **Clique Direito**: **LIMPA** todos os pontos do NPC selecionado.

## 📥 Instalação

1.  Baixe o arquivo `NPCMoverPlugin.jar`.
2.  Coloque na pasta `plugins` do seu servidor Paper/Spigot.
3.  Reinicie o servidor.
4.  Certifique-se de ter a permissão `npcmover.admin` para usar os comandos.

---
Desenvolvido com ❤️ para a comunidade de Minecraft.
