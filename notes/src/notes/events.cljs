(ns notes.events
  (:require
   [re-frame.core :as re-frame]
   [notes.db :as db]
   [central :as central]))

(defn json [obj] (js/JSON.stringify (clj->js obj)))

(defn request [url options]
  (central/Api.Blob.request url (clj->js options)))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::list-notes-complete
 (fn [db _]
   (do
     (request "/blobs/list" {:method "POST" :body (json {:bucket "jake.kinsella@gmail.com" :prefix "notes/"})})
     db)))

(re-frame/reg-event-db
 ::list-notes
 (fn [db _]
   (do (re-frame/dispatch [::list-notes-complete]) db)))
