# language: fr
Fonctionnalité: Administrateur - Authentification
  
  
  Scénario: Administrateur Mot de passe valide
    Soit Je suis sur la page de connexion
    Quand Je rentre les bons identifiants administateur
    Alors Je suis connecté en tant que "Administrateur"
    
  Scénario: Administrateur Mot de passe invalide
    Soit Je suis sur la page de connexion
    Quand Je rentre les mauvais identifiants administateur
    Alors J'ai un message d'erreur qui dit que les identifiants sont incorrects