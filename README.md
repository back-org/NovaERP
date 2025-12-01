# 🚀 NovaERP – Backend Facturation & ERP
### *Spring Boot 3 • REST API • JWT Security • Swagger/OpenAPI • Modular Architecture*

NovaERP est un backend ERP moderne proposant des modules professionnels de **facturation**, **gestion des clients**, **produits**, **paiements**, ainsi qu’un **dashboard analytique** destiné aux PME et cabinets professionnels.

Ce projet démontre une maîtrise avancée du développement backend avec **Java & Spring Boot**.

## 📌 Fonctionnalités principales
### 🧾 Module Facturation
- Création de facture (client + échéance + lignes d’articles)
- Calcul auto : HT, TVA, TTC, montant payé, montant restant
- Statuts automatiques : `BROUILLON`, `ENVOYEE`, `PAYEE`, `EN_RETARD`
- Numérotation automatique (ex. `FAC-2025-0001`)
- Ajout de paiements partiels ou compleplets
- Mise à jour automatique du statut selon les montants et échéances

### 👥 Module Clients
- CRUD complet
- Statut actif/inactif
- Historique des factures associées

### 📦 Module Produits / Services
- CRUD produits et services
- TVA paramétrable
- Prix HT, unité (heure, pièce, etc.)

### 💳 Module Paiements
Endpoint :  
`POST /api/factures/{id}/paiements`

### 📊 Module Dashboard
Endpoint : `/api/dashboard`

### 🔐 Sécurité (Spring Security 6 + JWT)
- Authentification : `POST /api/auth/login`

### 📘 Documentation API (Swagger/OpenAPI)
Swagger UI :  
`http://localhost:8080/swagger-ui.html`

## 👤 Auteur
Développé par **RANOELISON Dimbisoa Patrick**
