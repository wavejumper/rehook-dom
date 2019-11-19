(ns rehook.dom)

(defn ->html
  ([$ e]
   (list $ e))
  ([$ e props]
   (list $ e props))
  ([$ e props & children]
   (apply list $ e props (map (fn [x]
                                (if (sequential? x)
                                  (apply ->html $ x)
                                  x))
                              children))))

#?(:clj
   (defmacro html [$ component]
     `~(apply ->html $ component)))

#?(:clj
   (defmacro defui
     [name [ctx props $] & body]
     `(def ~name
        ^:rehook/component
        (fn [ctx# $#]
          (let [~ctx ctx#
                ~$ $#]
            (fn ~(gensym name) [props#]
              (let [~props props#]
                ~@body)))))))

#?(:clj
   (defmacro ui
     [[ctx props $] & body]
     `(with-meta
       (fn [ctx# $#]
         (let [~ctx ctx#
               ~$ $#]
           (fn [props#]
             (let [~props props#]
               ~@body))))
       {:rehook/component true})))
