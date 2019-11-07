(ns rehook.dom)

(defmacro defui
  [name [ctx props $] & body]
  `(doto (defn ~name [ctx# $#]
           (let [~ctx ctx#
                 ~$ $#]
             (fn ~(gensym name) [props#]
               (let [~props props#]
                 ~@body))))
     (aset "displayName" ~(str name))
     (aset "isRehookComponent" true)))

(defmacro ui
  [[ctx props $] & body]
  `(doto (fn [ctx# $#]
           (let [~ctx ctx#
                 ~$ $#]
             (fn [props#]
               (let [~props props#]
                 ~@body))))
     (aset "isRehookComponent" true)))
