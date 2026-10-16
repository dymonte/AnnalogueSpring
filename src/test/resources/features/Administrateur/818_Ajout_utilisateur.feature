# language: fr
Fonctionnalité: Administrateur - Ajout utilisateur
  
  
  Plan du Scénario: Administrateur ajout valide
    Soit Je suis connecté en tant que "Administrateur"
    Et L'utilisateur <login> n'existe pas
    Quand J'ajoute un utilisateur <login> en tant que <role> avec le mot de passe <mdp> avec comme année "<annee>"
    Alors L'utilisateur <login> existe
    Et <login> a le role <role>
    Et <login> a le mot de passe <mdp>
    Exemples:
    | login      | role                 | mdp           | annee         |
    | "respMod"  | "Responsable Module" | "RMd5dce4f8e" | 2027-2028     |
    | "memb"     | "Membre"             | "IMd5dce4f8e" | 2024-2025     |
  
  
  Scénario: Administrateur ajout d'un autre administrateur
    Soit Je suis connecté en tant que "Administrateur"
    Et L'utilisateur "admin2" n'existe pas
    Quand J'ajoute un utilisateur "admin2" en tant que "Administrateur"
    Alors J'ai un message d'erreur qui dit qu'on ne peut pas ajouter un autre administrateur
    
  
  
  Plan du Scénario: Administrateur utilisateur qui existe déjà
    Soit Je suis connecté en tant que "Administrateur"
    Et L'utilisateur <login> existe
    Quand J'ajoute un utilisateur <login> en tant que <role> avec le mot de passe <mdp> avec comme année "<annee>"
    Alors J'ai un message d'erreur qui dit que l'utilisateur existe déjà
    Exemples:
    | login      | role                 | mdp           | annee         |
    | "respMod"  | "Responsable Module" | "RMd5dce4f8e" | 2027-2028     |
    | "memb"     | "Membre"             | "IMd5dce4f8e" | 2024-2025     |
  
  Scénario: Administrateur role qui n'existe pas
    Soit Je suis connecté en tant que "Administrateur"
    Et L'utilisateur "boo" n'existe pas
    Quand J'ajoute un utilisateur "boo" en tant que "Fantome" avec le mot de passe "uiggiug54" avec comme année "2027-2028"
    Alors J'ai un message d'erreur qui dit que le role n'existe pas
  
  Plan du Scénario: Non administrateur
    Soit Je suis connecté en tant que <role>
    Et L'utilisateur <login> n'existe pas
    Quand J'ajoute un utilisateur <login> en tant que "Membre" avec le mot de passe "E5qq45e4fezz4f" avec comme année "2027-2028"
    Alors J'ai un message d'erreur qui dit que je n'ai pas le droit d'ajouter un utilisateur au site
    Exemples:
    | login      | role                 |
    | "respMod"  | "Responsable Module" |
    | "memb"     | "Membre"             |

    