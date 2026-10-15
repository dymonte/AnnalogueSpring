# language: fr
Fonctionnalité: Adminstrateur - gestion des roles
  
  
  Plan du Scénario: Administrateur changement de roles valides
    Soit Je suis connecté en tant que "Administrateur"
    Et <login> a le role <role1>
    Quand Je change le role de <login> pour le role <role2>
    Alors <login> a le role <role2>
    Exemples:
    | login  | role1               | role2               |
    | ERM    | Membre              | Responsable Module  |
  
  
  Plan du Scénario: Administrateur changement de roles administrateur
    Soit Je suis connecté en tant que "Administrateur"
    Et <login> a le role <role>
    Quand Je change le role de <login> pour le role "Administrateur"
    Alors J'ai un message d'erreur qui dit que je ne peux pas changer le role d'un utilisateur en Administrateur
    Exemples:
    | login       | role                 |
    | "RespMod"   | "Responsable Module" |
    | "memb"      | "Membre"             |
  
  Plan du Scénario: Administrateur changement de roles qui n'existe pas
    Soit Je suis connecté en tant que "Administrateur"
    Et <login> a le role <role>
    Quand Je change le role de <login> pour le role "Fantome"
    Alors J'ai un message d'erreur qui dit que le role n'existe pas
    Exemples:
    | login       | role                 |
    | "RespMod"   | "Responsable Module" |
    | "memb"      | "Membre"             |
  
  Plan du Scénario: Administrateur changement de roles utilisateur qui n'existe pas
    Soit Je suis connecté en tant que "Administrateur"
    Et L'utilisateur <login> n'existe pas
    Quand Je change le role de <login> pour le role <role>
    Alors J'ai un message d'erreur qui dit que l'utilisateur n'existe pas
    Exemples:
    | login       | role                 |
    | "RespMod"   | "Responsable Module" |
    | "memb"      | "Membre"             |
  
    
  
  
  Plan du Scénario: Non administrateur
    Soit Je suis connecté en tant que <role>
    Et <login> a le role "Membre"
    Quand Je change le role de <login> pour le role "Responsable Module"
    Alors J'ai un message d'erreur qui dit que je n'ai pas le droit de changer les roles des utilisateur
    Exemples:
    | login       | role                 |
    | "RespMod"   | "Responsable Module" |
    | "memb"      | "Membre"             |

  