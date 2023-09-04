(ns notes.views
  (:require
   [re-frame.core :as re-frame]
   [notes.styles :as styles]
   [notes.subs :as subs]
   [central :as central]))

(defn main []
  [:div
   [:h1
    {:class (styles/level1)}
    "Hello world"]
   ])

(def to_login (str central/Constants.central.root "/login?redirect=" (js/encodeURIComponent central/Constants.notes.root)))

(defn login []
  [:> central/Redirect {:to to_login}])

(def routes
  [["/"
    {:name ::main
     :view main}]

   ["/login"
    {:name ::login
     :view login}]])
