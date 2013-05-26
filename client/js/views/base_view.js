/**
 * Placeholder for placing common functionality from all views.
 * For now it is only trackAction (actually tracking what user doing in app).
 * Later can be added whatever we think is common
 * Author: bearz
 */
define(['underscore',
        'jquery',
        'backbone'],
    function(_, $, Backbone) {
        var BaseView = Backbone.View.extend({
            /**
             * Provide a convenient way to track all the user actions
             * @param user_id
             *       id of user to send to tracker
             * @type String
             * @param action_name
             *      string name of action
             * @type String
             * @param options
             *      object with options to be tracked. optional
             * @type Object
             *
             */
            trackUserAction: function(user_id, action_name, options) {
                console.log("User with id: " + user_id + " did action: " + action_name);
                // TODO connect a real tracker.
            }
        });

        return BaseView;
    }
);