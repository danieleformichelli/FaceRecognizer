APPLICATION DESIGN
==================

Componenti dell'app
-------------------

### FaceRecognitionFragment


Attività che mostra il video della fotocamera, ci sovrappone l'etichetta/quadratino della faccia se trovata e riconosciuta.

Da qui si può navigare verso l'attività di training **FaceDetectionFragment** e verso la **SettingsFragment**.

### FaceDetectionFragment

Chiama l'attività android per fare la foto. Se non ci sono facce nella foto, richiedere un'altra foto all'utente.

Una volta fatta la foto, l'utente sceglie una faccia tra quelle trovate. (filtra i false positive)

Poi vengono estratte le features e vengono ritornate al chiamante (insieme ad altre cose utili: thumbnail etc..)

### FacesManagementFragment

Lista coi nomi delle facce riconosciute, possibiltà di aggiungere (vedi **FaceDetectionFragment**) o rimuovere facce/nomi.
	
### SettingsFragment
	
Attività per i settaggi. Per ora contiene:

- eventuali parametri del detection e del recognition
- ??? ancora non saprei

### FacesContentProvider (o FaceDB)

Database delle facce. Deve poter gestire le seguenti "tabelle":
	
	People: < person_id, nome, [eventiali_dati_di_un_nome] >
	Faces: < face_id, person_id, thumbnail, features >
	
Note
----

Si pensava di usare i fragment invece che le activity perchè l'ADT rende automatico fare una UI ganza con navigazione a swipe e altre cose carine che praticamente vengono gratis coi fragment!

La fregatura è che dobbiamo stare attenti ad allocare e deallocare le risorse (Fotocamera, Servizi di OpenCV, etc..) nella lifecycle dei fragment altrimenti si potrebbe compromettere tutta l'activity (cioè tutta l'app)!

