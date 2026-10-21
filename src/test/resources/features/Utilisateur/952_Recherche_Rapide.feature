# language: fr
Fonctionnalité: Utilisateur - Recherche Rapide
  
  
  Scénario: Recherche rapide d'un document accessible
    Soit l'utilisateur "ToTo" est inscrit au site
    Et "ToTo" a accès au module "MOIA"
    Et le document "Labyrinthe en Long et en Largeur" est disponible dans le module "MOIA"
    Quand je me connecte avec l'identifiant "ToTo"
    Et je recherche "Labyrinthe en Long et en Largeur"
    Alors le document "Labyrinthe en Long et en Largeur" est affiché dans les résultats
  
  
  Scénario: Recherche rapide d'un document inaccessible
    Soit l'utilisateur "ToTo" est inscrit au site
    Et "ToTo" n'a pas accès au module "MOIA"
    Et le document "Labyrinthe en Long et en Largeur" est disponible dans le module "MOIA"
    Quand je me connecte avec l'identifiant "ToTo"
    Et je recherche "Labyrinthe en Long et en Largeur"
    Alors le document "Labyrinthe en Long et en Largeur" n'est pas affiché dans les résultats
    
  Scénario: Recherche rapide sans résultat
    Soit l'utilisateur "ToTo" est inscrit au site
    Et aucun document accessible à "ToTo" ne contient le mot "Labyrinthe"
    Quand je me connecte avec l'identifiant "ToTo"
    Et je recherche "Labyrinthe"
    Alors aucun document n'est affiché dans les résultats