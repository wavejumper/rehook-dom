(ns rehook.dom)

(defn eval-hiccup
  ([$ e]
   ($ e))
  ([$ e props]
   ($ e props))
  ([$ e props & children]
   (apply $ e props (keep (fn [x]
                            (if (vector? x)
                              (apply eval-hiccup $ x)
                              x))
                          children))))

(defn compile-hiccup
  ([$ e]
   (list $ e))
  ([$ e props]
   (list $ e props))
  ([$ e props & children]
   (apply list $ e props (keep (fn [x]
                                 (cond
                                   (vector? x)
                                   (apply compile-hiccup $ x)

                                   (or (nil? x) (string? x) (number? x))
                                   x

                                   :else `(apply eval-hiccup ~$ ~x)))
                              children))))

#?(:clj
   (defmacro html [$ component]
     (if (vector? component)
       `~(apply compile-hiccup $ component)
       `(apply eval-hiccup ~$ ~component))))

#?(:clj
   (defmacro defui
     [name [ctx props $?] & body]
     (if $?
       `(def ~name
          ^{:rehook/component true
            :rehook/name      ~(str name)}
          (fn [ctx# $#]
            (let [~ctx ctx#
                  ~$? $#]
              (fn ~(gensym name) [props#]
                (let [~props props#]
                  ~@body)))))

       `(def ~name
          ^{:rehook/component true
            :rehook/name      ~(str name)}
          (fn [ctx# $#]
            (let [~ctx ctx#
                  ~'&$ $#]
              (fn ~(gensym name) [props#]
                (let [~props props#]
                  (html ~'&$ ~@body)))))))))

#?(:clj
   (defmacro ui
     [[ctx props $?] & body]
     (if $?
       (let [id (gensym "ui")]
         `(with-meta
           (fn ~id [ctx# $#]
             (let [~ctx ctx#
                   ~$? $#]
               (fn [props#]
                 (let [~props props#]
                   ~@body))))
           {:rehook/component true
            :rehook/name      ~(str id)}))

       (let [id (gensym "ui")]
         `(with-meta
           (fn ~id [ctx# $#]
             (let [~ctx ctx#
                   ~'&$ $#]
               (fn [props#]
                 (let [~props props#]
                   (html ~'&$ ~@body)))))
           {:rehook/component true
            :rehook/name      ~(str id)})))))

