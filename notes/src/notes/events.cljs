(ns notes.events
  (:require
    [re-frame.core :as re-frame]
    [notes.db :as db]
    [notes.api :as api]
    [central :as central]))

(defn reg-event-with-user [event fun]
  (re-frame/reg-event-db
    event
    (fn [db x]
      (if (nil? (:user db))
        (do (re-frame/dispatch [::get-user x]) db)
        (fun db x)))))


(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))


(re-frame/reg-event-db
 ::set-error
 (fn [db [_ error]]
   (assoc db :error error)))


(re-frame/reg-event-db
 ::get-user-complete
 (fn [db [_ user after]]
   (do
     (if (not (nil? user)) (re-frame/dispatch after))
     (assoc db :user user))))

(re-frame/reg-event-db
 ::get-user
 (fn [db [_ after]]
   (do
     (.then (api/get-user)
       #(re-frame/dispatch [::get-user-complete % after]))
     db)))


(re-frame/reg-event-db
 ::list-notes-complete
 (fn [db [_ notes]]
   (assoc db :notes notes)))

(reg-event-with-user
 ::list-notes
 (fn [db _]
   (do
     (.then (api/list-notes (:email (:user db)))
         #(re-frame/dispatch [::list-notes-complete %]))
     db)))


(re-frame/reg-event-db
 ::select-note-clear
 (fn [db _]
   (assoc db :selected nil)))

(re-frame/reg-event-db
 ::select-note-complete
 (fn [db [_ note]]
   (assoc db :selected note)))

(reg-event-with-user
 ::select-note
 (fn [db [_ title]]
   (do
     (->
       (api/get-note (:email (:user db)) title)
       (.then #(re-frame/dispatch [::select-note-complete %])))
     db)))


(re-frame/reg-event-db
 ::save-note-complete
 (fn [db [_ after]]
   (do
     (re-frame/dispatch [::list-notes])
     (re-frame/dispatch after)
     db)))

(reg-event-with-user
 ::save-note
 (fn [db [_ title body after]]
   (do
     (->
       (api/create-note (:email (:user db)) title "")
       (.then #(re-frame/dispatch [::save-note-complete after]))
       (.catch #(re-frame/dispatch [::set-error "Invalid title"])))
     db)))


(re-frame/reg-event-db
 ::dialog-open
 (fn [db [_ dialog]]
   (assoc db :dialog dialog)))

(re-frame/reg-event-db
 ::dialog-close
 (fn [db _]
   (assoc db :dialog nil :error nil)))
