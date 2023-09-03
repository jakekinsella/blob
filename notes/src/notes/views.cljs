(ns notes.views
  (:require
   [re-frame.core :as re-frame]
   [notes.styles :as styles]
   [notes.subs :as subs]
   ))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1
      {:class (styles/level1)}
      "Hello from " @name]
     ]))
