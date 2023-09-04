(ns notes.views
  (:require
   [re-frame.core :as re-frame]
   [notes.styles :as styles]
   [notes.subs :as subs]
   ))

(defn main []
  [:div
   [:h1
    {:class (styles/level1)}
    "Hello world"]
   ])

(defn login []
  [:div "login"])

(def routes
  [["/"
    {:name ::main
     :view main}]

   ["/login"
    {:name ::login
     :view login}]])
