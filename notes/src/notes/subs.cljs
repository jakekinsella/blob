(ns notes.subs
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::notes
 (fn [db]
   (-> db :notes :notes)))

(re-frame/reg-sub
 ::selected
 (fn [db]
   (:selected db)))

(re-frame/reg-sub
 ::dialog
 (fn [db]
   (:dialog db)))

(re-frame/reg-sub
 ::error
 (fn [db]
   (:error db)))
