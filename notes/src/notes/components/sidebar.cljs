(ns notes.components.sidebar
  (:require
    [re-frame.core :as re-frame]))

(defn container [children] (into [:div] children))
(defn header [children] (into [:div] children))
(defn spacer [] [:div])
(defn item [children] (into [:div] children))

(defn build [notes]
  (container [(header "Notes")
              (spacer)
              [:div (item "test")]]))
