# language: fr
Fonctionnalité: Administrateur - Déconnexion
  
  Scénario: Administrateur déconnexion
    Soit Je suis connecté en tant que "Administrateur"
    Quand Je me déconnecte
    Alors Je ne suis pas connecté
    
  Plan du Scénario: Administrateur ajout non connecté
    Soit Je suis connecté en tant que "Administrateur"
    Et L'utilisateur <login> n'existe pas
    Quand Je me déconnecte
    Et J'ajoute un utilisateur <login> en tant que <role> avec le mot de passe <mdp> avec comme année "<annee>"
    Alors Je reçoit un message d'erreur
    Exemples:
    | login      | role                 | mdp           | annee         |
    | "respMod"  | "Responsable Module" | "RMd5dce4f8e" | 2027-2028     |
    | "memb"     | "Membre"             | "IMd5dce4f8e" | 2024-2025     |
  
  Plan du Scénario: Administrateur supression non connecté
    Soit Je suis connecté en tant que "Administrateur"
    Et <login> a le role <role>
    Quand Je me déconnecte
    Et Je supprime <login> du site
    Alors Je reçoit un message d'erreur
    Exemples:
    | login       | role                  |
    | "RespMod"   | "Responsable Module"  |
    | "memb"      | "Membre"              |

  Plan du Scénario: Administrateur changement de roles non connecté
    Soit Je suis connecté en tant que "Administrateur"
    Et <login> a le role <role1>
    Quand Je me déconnecte
    Et Je change le role de <login> pour le role <role2>
    Alors Je reçoit un message d'erreur
    Exemples:
    | login  | role1               | role2               |
    | ERM    | Membre              | Responsable Module  |