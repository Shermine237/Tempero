# Tempero - Application de Gestion Intelligente du Temps

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Plateforme](https://img.shields.io/badge/plateforme-Android-brightgreen.svg)
![Langage](https://img.shields.io/badge/langage-Java-orange.svg)

## 📱 Présentation

Tempero est une application mobile intelligente conçue pour optimiser la gestion du temps grâce à l'intelligence artificielle. Contrairement aux applications traditionnelles de type To-Do List, Tempero analyse vos habitudes de travail, apprend de vos comportements et vous propose des plannings personnalisés et optimisés.

## 🌟 Fonctionnalités

### Gestion des Tâches
- Création, modification et suppression de tâches
- Définition des priorités, difficultés et durées estimées
- Catégorisation des tâches (travail, personnel, études, etc.)
- Suivi de l'avancement et des tâches complétées

### Planification Intelligente
- Génération automatique de plannings optimisés
- Prise en compte des priorités et des échéances
- Insertion intelligente de pauses
- Adaptation aux imprévus et replanification dynamique

### Analyse des Habitudes
- Détection des heures et jours les plus productifs
- Identification des catégories de tâches où vous êtes le plus efficace
- Prédiction de la durée réelle des tâches
- Conseils personnalisés pour améliorer votre productivité

### Statistiques et Rapports
- Suivi de votre productivité au fil du temps
- Visualisation de votre taux de complétion des tâches
- Analyse de l'efficacité par catégorie
- Conseils d'amélioration basés sur vos données

### Personnalisation
- Définition de vos heures et jours de travail préférés
- Configuration des durées de pause
- Création de catégories personnalisées
- Adaptation aux préférences individuelles

## 🛠️ Architecture Technique

Tempero est développée selon l'architecture MVVM (Model-View-ViewModel) et utilise les composants Android Jetpack :

- **Interface Utilisateur** : Navigation Component, RecyclerView, ViewBinding
- **Logique Métier** : ViewModel, LiveData
- **Persistance des Données** : Room Database
- **Intelligence Artificielle** : Algorithmes d'apprentissage et d'optimisation personnalisés

## 📊 Composants d'IA

L'application intègre plusieurs composants d'intelligence artificielle :

1. **UserHabitAnalyzer** : Analyse vos habitudes de travail et identifie vos patterns de productivité
2. **IntelligentScheduler** : Génère des plannings optimisés en fonction de vos habitudes et contraintes
3. **AIService** : Coordonne les différents composants d'IA et fournit des recommandations

## 🚀 Installation

### Prérequis
- Android Studio (version 2023.2.1 ou supérieure)
- JDK 11 ou supérieur
- Un appareil ou émulateur Android avec API 24 minimum (Android 7.0 Nougat)

### Étapes d'installation
1. Clonez ce dépôt :
   ```
   git clone https://github.com/Shermine237/Tempero.git
   ```
2. Ouvrez le projet dans Android Studio
3. Synchronisez le projet avec les fichiers Gradle
4. Exécutez l'application sur un émulateur ou un appareil physique

## 📝 Guide d'utilisation

### Premier démarrage
Lors du premier lancement, l'application crée automatiquement un profil utilisateur par défaut. Vous pouvez le personnaliser dans l'onglet "Profil".

### Création de tâches
1. Accédez à l'onglet "Tâches"
2. Appuyez sur le bouton "+" en bas à droite
3. Remplissez les détails de la tâche (titre, description, échéance, etc.)
4. Appuyez sur "Enregistrer"

### Génération de planning
1. Accédez à l'onglet "Planning"
2. Sélectionnez une date dans le calendrier
3. Appuyez sur le bouton de synchronisation en bas à droite
4. L'IA générera un planning optimisé pour cette journée

### Consultation des statistiques
1. Accédez à l'onglet "Statistiques"
2. Consultez vos données de productivité
3. Appuyez sur "Rafraîchir les statistiques" pour mettre à jour l'analyse

### Personnalisation du profil
1. Accédez à l'onglet "Profil"
2. Modifiez vos informations personnelles
3. Définissez vos préférences de travail et de pause
4. Appuyez sur "Enregistrer les modifications"

## 🔧 Personnalisation avancée

### Catégories personnalisées
Vous pouvez créer vos propres catégories de tâches dans la section Profil. Ces catégories seront disponibles lors de la création de nouvelles tâches.

### Préférences de travail
Définissez vos heures de travail préférées et vos jours de disponibilité pour que l'IA puisse générer des plannings adaptés à votre rythme.

### Préférences de pause
Configurez la durée de vos pauses courtes et longues, ainsi que le nombre de sessions de travail avant une pause longue.

## 📈 Évolutions futures

Voici les fonctionnalités prévues pour les prochaines versions :

- Synchronisation avec des calendriers externes (Google Calendar, Outlook)
- Intégration avec des assistants vocaux
- Mode hors-ligne amélioré
- Visualisations graphiques avancées des statistiques
- Partage de tâches et collaboration
- Version web et synchronisation multi-appareils

## 👥 Équipe de développement

Ce projet a été développé par :
- DOUMI Serge (Superviseur)
- NBOBO SOPPO Lucia Clara
- EMANI YOSSA Charlie Rostant

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.

## 📞 Contact

Pour toute question ou suggestion, veuillez contacter l'équipe à l'adresse : tempero.app@example.com

---

© 2025 Tempero. Tous droits réservés.
