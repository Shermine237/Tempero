# Tempero - Application de Gestion Intelligente du Temps

![Version](https://img.shields.io/badge/version-1.1.0-blue.svg)
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
- Système d'approbation des tâches générées par l'IA

### Planification Intelligente
- Génération automatique de plannings optimisés
- Prise en compte des priorités et des échéances
- Insertion intelligente de pauses
- Adaptation aux imprévus et replanification dynamique
- Intégration des données météorologiques pour les activités extérieures
- Prise en compte des événements du calendrier pour éviter les conflits

### Analyse des Habitudes
- Détection des heures et jours les plus productifs
- Identification des catégories de tâches où vous êtes le plus efficace
- Prédiction de la durée réelle des tâches
- Conseils personnalisés pour améliorer votre productivité
- Reconnaissance de modèles récurrents dans vos activités
- Analyse des taux de réussite par type de tâche

### Statistiques et Rapports
- Suivi de votre productivité au fil du temps
- Visualisation de votre taux de complétion des tâches
- Analyse de l'efficacité par catégorie
- Conseils d'amélioration basés sur vos données
- Distinction entre tâches manuelles et tâches générées par l'IA

### Personnalisation
- Définition de vos heures et jours de travail préférés
- Configuration des durées de pause
- Création de catégories personnalisées
- Adaptation aux préférences individuelles
- Prise en compte de votre localisation pour les suggestions de tâches

### Contexte Intelligent
- Adaptation des suggestions en fonction de la météo
- Intégration avec votre calendrier pour éviter les conflits
- Suggestions basées sur votre localisation actuelle
- Descriptions de tâches enrichies avec des informations contextuelles

## 🛠️ Architecture Technique

Tempero est développée selon l'architecture MVVM (Model-View-ViewModel) et utilise les composants Android Jetpack :

- **Interface Utilisateur** : Navigation Component, RecyclerView, ViewBinding
- **Logique Métier** : ViewModel, LiveData
- **Persistance des Données** : Room Database
- **Intelligence Artificielle** : Algorithmes d'apprentissage et d'optimisation personnalisés

## 📊 Composants d'IA

L'application intègre plusieurs composants d'intelligence artificielle avancés :

1. **UserHabitAnalyzer** : Analyse vos habitudes de travail et identifie vos patterns de productivité
2. **IntelligentScheduler** : Génère des plannings optimisés en fonction de vos habitudes et contraintes
3. **AIService** : Coordonne les différents composants d'IA et fournit des recommandations
4. **TaskPatternRecognizer** : Identifie des modèles récurrents dans vos tâches
5. **WeatherService** : Intègre les données météorologiques dans la planification
6. **CalendarIntegrationService** : Synchronise avec votre calendrier
7. **LocationService** : Utilise votre position pour des suggestions contextuelles

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
5. Approuvez ou modifiez les tâches générées par l'IA

### Consultation des statistiques
1. Accédez à l'onglet "Statistiques"
2. Consultez vos données de productivité
3. Appuyez sur "Rafraîchir les statistiques" pour mettre à jour l'analyse
4. Visualisez la distinction entre tâches manuelles et tâches IA

### Personnalisation du profil
1. Accédez à l'onglet "Profil"
2. Modifiez vos informations personnelles
3. Définissez vos préférences de travail et de pause
4. Configurez vos localisations habituelles (domicile, travail)
5. Appuyez sur "Enregistrer les modifications"

## 🔧 Personnalisation avancée

### Catégories personnalisées
Vous pouvez créer vos propres catégories de tâches dans la section Profil. Ces catégories seront disponibles lors de la création de nouvelles tâches.

### Préférences de travail
Définissez vos heures de travail préférées et vos jours de disponibilité pour que l'IA puisse générer des plannings adaptés à votre rythme.

### Préférences de pause
Configurez la durée de vos pauses courtes et longues, ainsi que le nombre de sessions de travail avant une pause longue.

### Intégration contextuelle
Autorisez l'accès à la localisation et au calendrier pour des suggestions plus pertinentes.

## 🧠 Intelligence Artificielle

### Apprentissage des habitudes
L'IA analyse vos actions (tâches complétées, reportées, etc.) pour apprendre vos préférences et habitudes de travail.

### Reconnaissance de modèles
L'application identifie automatiquement les modèles récurrents dans vos activités :
- Modèles quotidiens (tâches que vous faites à certaines heures)
- Modèles hebdomadaires (activités liées à certains jours)
- Modèles mensuels (tâches récurrentes chaque mois)

### Suggestions contextuelles
Les suggestions de tâches prennent en compte :
- La météo (pas d'activités extérieures sous la pluie)
- Votre calendrier (évite les conflits avec vos rendez-vous)
- Votre localisation (tâches adaptées à votre position actuelle)
- Vos taux de réussite passés (priorise les tâches que vous complétez habituellement)

## 📈 Évolutions futures

Voici les fonctionnalités prévues pour les prochaines versions :

- Synchronisation avec des calendriers externes (Google Calendar, Outlook)
- Intégration avec des assistants vocaux
- Mode hors-ligne amélioré
- Visualisations graphiques avancées des statistiques
- Partage de tâches et collaboration
- Version web et synchronisation multi-appareils
- Intégration avec des services de météo en temps réel
- Suggestions basées sur l'actualité et les événements locaux

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

&#169; 2025 Tempero. Tous droits réservés.
