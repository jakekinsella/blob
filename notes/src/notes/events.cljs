(ns notes.events
  (:require
   [re-frame.core :as re-frame]
   [notes.db :as db]
   [notes.api :as api]
   [central :as central]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::get-user-complete
 (fn [db user]
   (assoc db :user user)))

(re-frame/reg-event-db
 ::get-user
 (fn [db _]
   (do
     (.then (api/get-user)
       #(re-frame/dispatch [::get-user-complete %]))
     db)))

(re-frame/reg-event-db
 ::list-notes-complete
 (fn [db [_ notes]]
   (assoc db :notes notes)))

(re-frame/reg-event-db
 ::list-notes
 (fn [db _]
   (do
     (.then (api/list-notes "jake.kinsella@gmail.com")
       #(re-frame/dispatch [::list-notes-complete %]))
     db)))
