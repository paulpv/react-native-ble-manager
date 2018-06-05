/**
 * From https://github.com/helpdotcom/timeout-manager.js/blob/master/timeout-manager.js
 * Other ideas:
 * https://github.com/xushunke/TimeoutObject/blob/master/index.js
 */

'use strict';

  // ## TimeoutManager
  // This timeout manager provides a simple timeout container.  Functions are
  // added with a unique key and executed when the configured timeout period
  // expires.  The timeouts can be cleared as well by their key.
  //
  //     var TimeoutManager = require('timeout-manager');
  //     var timeoutManager = new TimeoutManager();
  //     timeoutManager.add('greet', function() {
  //       console.log('Hello there!');
  //     });

  // ### TimeoutManager *constructor*
  // Initiate the timeout manager with an optional timeout period (default is
  // 60000, or 60s).
  //
  //     var timeoutManager = new TimeoutManager({timeout: 10000});
  var TimeoutManager = function(opts) {
    this._timeout = opts.timeout || 60000;
    this._list = {};
  };

  // ### TimeoutManager.add
  // Starts a timer that will execute the given function when time is done.
  // The timer is stored by the given id which can be used to stop the timer.
  // If this is called again with the same key before the previous timer has
  // fired, this will remove the existing timer and start a new one with the
  // new function.
  //
  //     timeoutManager.add('greet', function() {
  //       console.log('Hello there!');
  //     });
  TimeoutManager.prototype.add = function(key, fn) {
    if (this._list.hasOwnProperty(key)) {
      this.remove(key);
    }

    var self = this;
    this._list[key] = setTimeout(function() {
      self.remove(key);
      fn(key, self._timeout);
    }, this._timeout);
  };

  // ### TimeoutManager.remove
  // Stops the timer with the given key and removes it from the manager.  If
  // the timer with the given key does not exist or has already fired, this
  // returns without error.
  //
  //     timeoutManager.remove('greet');
  TimeoutManager.prototype.remove = function(key) {
    if (!this._list.hasOwnProperty(key)) {
      return;
    }

    clearTimeout(this._list[key]);
    delete this._list[key];
  };

  export default TimeoutManager;