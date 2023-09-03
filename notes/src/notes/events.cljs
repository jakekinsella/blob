(ns notes.events
  (:require
   [re-frame.core :as re-frame]
   [notes.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))
