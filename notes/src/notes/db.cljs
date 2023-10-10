(ns notes.db)

(def default-db
  {:user nil
   :notes {:loaded false :notes []}
   :selected nil
   :dialog nil
   :error nil
   :sidebar-open? true
   :drawing "pen"})
