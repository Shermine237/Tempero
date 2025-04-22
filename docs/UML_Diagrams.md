# Documentation UML de l'Application Tempora

## 1. Diagramme de Cas d'Utilisation

```plantuml
@startuml
left to right direction
actor "Utilisateur" as user
actor "Système IA" as ai

rectangle "Application Tempora" {
  usecase "Gérer les tâches" as UC1
  usecase "Consulter le planning" as UC2
  usecase "Personnaliser les préférences" as UC3
  usecase "Analyser les statistiques" as UC4
  usecase "Générer des suggestions" as UC5
  usecase "Gérer le profil" as UC6
  usecase "Approuver les tâches IA" as UC7
  usecase "Analyser les habitudes" as UC8
  usecase "Vérifier la météo" as UC9
  usecase "Vérifier le calendrier" as UC10
  usecase "Analyser la localisation" as UC11
}

user --> UC1
user --> UC2
user --> UC3
user --> UC4
user --> UC6
user --> UC7

ai --> UC5
ai --> UC8
ai --> UC9
ai --> UC10
ai --> UC11

UC5 ..> UC8 : <<include>>
UC5 ..> UC9 : <<include>>
UC5 ..> UC10 : <<include>>
UC5 ..> UC11 : <<include>>
@enduml
```

## 2. Diagramme de Classes

```plantuml
@startuml
package "Model" {
  class Task {
    -title: String
    -description: String
    -dueDate: Date
    -priority: int
    -category: String
    -completed: boolean
    +getTitle()
    +setTitle()
    +complete()
    +postpone()
  }
  
  class UserProfile {
    -name: String
    -preferences: UserPreferences
    -workHours: WorkHours
    +updatePreferences()
    +getWorkHours()
  }
  
  class Schedule {
    -date: Date
    -items: List<ScheduleItem>
    +addItem()
    +removeItem()
    +optimize()
  }
}

package "AI" {
  class AIBackendService {
    -habitAnalyzer: UserHabitAnalyzer
    -weatherService: WeatherService
    -calendarService: CalendarIntegrationService
    -locationService: LocationService
    +generateTasks()
    +analyzeTasks()
    +predictDuration()
  }
  
  class UserHabitAnalyzer {
    -activities: List<UserActivity>
    +analyzePatterns()
    +predictTaskDuration()
    +getTaskSuccessRate()
  }
  
  class TaskPatternRecognizer {
    +identifyPatterns()
    +suggestTasks()
  }
}

package "Service" {
  class AIService {
    -backend: AIBackendService
    +generateSuggestions()
    +optimizeSchedule()
  }
  
  class NotificationService {
    +sendReminder()
    +notifyChanges()
  }
}

Task "1" -- "1..*" Schedule
UserProfile "1" -- "1" WorkHours
AIBackendService "1" -- "1" UserHabitAnalyzer
AIBackendService "1" -- "1" TaskPatternRecognizer
AIService "1" -- "1" AIBackendService
@enduml
```

## 3. Diagramme de Séquence

```plantuml
@startuml
actor User
participant "UI" as ui
participant "TaskViewModel" as vm
participant "AIService" as ai
participant "AIBackendService" as backend
participant "UserHabitAnalyzer" as analyzer
participant "WeatherService" as weather
participant "CalendarService" as calendar

User -> ui : Demande génération planning
activate ui
ui -> vm : generateSchedule()
activate vm
vm -> ai : generateSuggestions()
activate ai

ai -> backend : analyzeTasks()
activate backend
backend -> analyzer : analyzePatterns()
activate analyzer
analyzer --> backend : patterns
deactivate analyzer

backend -> weather : getWeatherForecast()
activate weather
weather --> backend : forecast
deactivate weather

backend -> calendar : checkConflicts()
activate calendar
calendar --> backend : conflicts
deactivate calendar

backend --> ai : suggestions
deactivate backend

ai --> vm : optimizedSchedule
deactivate ai

vm --> ui : updatedSchedule
deactivate vm

ui --> User : Affiche planning
deactivate ui
@enduml
```

## 4. Diagramme d'Activités

```plantuml
@startuml
start
:Utilisateur demande un planning;

fork
  :Analyser habitudes utilisateur;
  :Identifier patterns récurrents;
fork again
  :Vérifier météo;
fork again
  :Vérifier calendrier;
fork again
  :Vérifier localisation;
end fork

:Générer suggestions de tâches;

if (Conflits détectés?) then (oui)
  :Ajuster planning;
else (non)
  :Conserver planning initial;
endif

:Présenter planning à l'utilisateur;

if (Utilisateur approuve?) then (oui)
  :Enregistrer planning;
  :Créer notifications;
else (non)
  :Permettre modifications;
  :Enregistrer feedback;
endif

stop
@enduml
```

## 5. Diagramme d'États

```plantuml
@startuml
[*] --> NonPlanifiée

state NonPlanifiée {
  [*] --> EnAttente
  EnAttente --> Suggérée : IA génère suggestion
}

state Planifiée {
  Suggérée --> Approuvée : Utilisateur approuve
  Approuvée --> EnCours : Heure de début
  EnCours --> Complétée : Utilisateur termine
  EnCours --> Reportée : Utilisateur reporte
}

Reportée --> NonPlanifiée : Replanification
Complétée --> [*]

@enduml
```

## 6. Diagramme de Composants

```plantuml
@startuml
package "UI Layer" {
  [Activities]
  [Fragments]
  [ViewModels]
}

package "Business Layer" {
  [AIService]
  [TaskManager]
  [NotificationService]
}

package "AI Layer" {
  [AIBackendService]
  [PatternRecognizer]
  [HabitAnalyzer]
}

package "External Services" {
  [WeatherService]
  [CalendarService]
  [LocationService]
}

database "Local Storage" {
  [Room Database]
}

[Activities] --> [ViewModels]
[ViewModels] --> [AIService]
[AIService] --> [AIBackendService]
[AIBackendService] --> [PatternRecognizer]
[AIBackendService] --> [HabitAnalyzer]
[AIBackendService] --> [WeatherService]
[AIBackendService] --> [CalendarService]
[AIBackendService] --> [LocationService]
[TaskManager] --> [Room Database]
@enduml
```

## 7. Diagramme de Déploiement

```plantuml
@startuml
node "Appareil Android" {
  artifact "Application Tempora" {
    component [UI Layer]
    component [Business Layer]
    component [AI Layer]
    database "SQLite DB"
  }
}

cloud "Services Externes" {
  [Service Météo]
  [Service Calendrier]
  [Service Localisation]
}

[AI Layer] --> [Service Météo] : HTTPS
[AI Layer] --> [Service Calendrier] : HTTPS
[AI Layer] --> [Service Localisation] : HTTPS
[Business Layer] --> [SQLite DB]
@enduml
```

## Légende et Notes

### Conventions de couleurs
- Bleu : Composants système
- Vert : Actions utilisateur
- Rouge : Points critiques/erreurs
- Jaune : Processus d'IA

### Notes importantes
1. Les diagrammes sont basés sur l'implémentation actuelle de l'application
2. Les relations et dépendances reflètent l'architecture MVVM
3. L'accent est mis sur l'intégration des composants d'IA
4. Les services externes sont représentés de manière simplifiée
