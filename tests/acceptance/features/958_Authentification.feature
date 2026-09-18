# language: fr
Fonctionnalité: Authentification
  
  
  Plan du scénario: 
    Soit L'utilisateur "ToTo" possède un compte <role> ayant comme mot de passe "titi" 
    Quand je m'authentifie avec l'identifiant "ToTo" et le mot de passe "titi"
    Alors je suis connecté en tant que "ToTo" avec le rôle <role>
    Exemples: :
    | role                    |
    | "Administrateur"        |
    | "Responsable Module"    |
    | "Membre"                |
    
    
  Scénario: Connexion sans compte
    Soit L'utilisateur "ToTo" n'existe pas 
    Quand je m'authentifie avec l'identifiant "ToTo"
    Alors j'ai un message d'erreur
    
    
  
  Plan du scénario: 
    Soit L'utilisateur "ToTo" possède un compte <role> ayant comme mot de passe "titi"
    Quand je m'authentifie avec l'identifiant "ToTo" et le mot de passe "tutu"
    Alors j'ai un message d'erreur
    Exemples: :
    | role                    |
    | "Administrateur"        |
    | "Responsable Module"    |
    | "Membre"                |
  
  
    