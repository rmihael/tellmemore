/**
 * Contains a basic app bone. Menus/main container/switching views logic must be placed
 * here
 * Author: bearz
 */
define([
    'underscore',
    'backbone',
    'jquery',
    'base_view',
    'text!templates/app_template.html',
    'views/search_view'
], function(_, Backbone, $, BaseView, AppTemplate, SearchView) {
    var AppView = BaseView.extend({
        el: "body",
        template: _.template(AppTemplate),
        initialize: function() {
            this.search_components = []; // will be used for gathering data later
        },
        /**
         * Here is the basic rendering happened.
         * App View renders itself DOM document and
         * then ready to command any child view
         */
        render: function() {
            this.$el.html(this.template());
            // creating search view and specifying element for it
            this.search_view = new SearchView({el: "#module_container"});
            this.search_view.render();

            return this;
        }
    });

    return AppView;
});
