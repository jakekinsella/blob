(ns notes.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::notes
 (fn [db]
   (:notes db)))
