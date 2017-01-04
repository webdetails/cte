/*!
 * Copyright 2002 - 2017 Webdetails, a Pentaho company. All rights reserved.
 *
 * This software was developed by Webdetails and is provided under the terms
 * of the Mozilla Public License, Version 2.0, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

// Based on https://github.com/miguelleite/ml-modal

// Define constructor
var Modal = function() {

  // Create global element references
  this.closeButton = null;
  this.modal = null;
  this.overlay = null;
  this.header = null;
  this.headerIcon = null;
  this.confirmDialog = null;
  this.saveBtn = null;
  this.dontSaveBtn = null;
  this.cancelBtn = null;

  // Determine proper prefix
  this.transitionEnd = transitionSelect();

  // Define option defaults
  var defaults = {
    className: 'fade-and-drop',
    closeButton: true,
    content: "",
    maxWidth: 600,
    minWidth: 280,
    overlay: true,
    autoOpen: false
  };

  // Create options by extending defaults with the passed in arguments
  if (arguments[0] && typeof arguments[0] === "object") {
    this.options = extendDefaults(defaults, arguments[0]);
  }

};

// Public Methods

// Method to open modal
Modal.prototype.open = function() {

  // Build out our modal
  buildOut.call(this);

  // Initialize our event listeners
  initializeEvents.call(this);

  /*
   * After adding elements to the DOM, use getComputedStyle
   * to force the browser to recalc and recognize the elements
   * that we just added. This is so that CSS animation has a start point.
   */
  window.getComputedStyle(this.modal).height;

  /*
   * Add our open class and check if the modal is taller than the window.
   * If so, our anchored class is also applied.
   */
  this.modal.className = this.modal.className + (this.modal.offsetHeight > window.innerHeight ? " ml-open ml-anchored" : " ml-open");
  this.overlay.className = this.overlay.className + " ml-open";
};

Modal.prototype.close = function() {

  // Store the value of this
  var _ = this;

  // Remove the open class name
  this.modal.className = this.modal.className.replace(" ml-open", "");
  this.overlay.className = this.overlay.className.replace(" ml-open", "");

  // Listen for CSS transitionEnd event and then remove the nodes from the DOM
  this.modal.addEventListener(this.transitionEnd, function() {
    _.modal.parentNode.removeChild(_.modal);
  });
  this.overlay.addEventListener(this.transitionEnd, function() {
    if (_.overlay.parentNode) _.overlay.parentNode.removeChild(_.overlay);
  })

};

Modal.prototype.closeConfirm = function() {

  // Store the value of this
  var _ = this;

  // Remove the open class name
  this.modal.className = this.modal.className.replace(" ml-open", "");
  this.overlay.className = this.overlay.className.replace(" ml-open", "");

  // Listen for CSS transitionEnd event and then remove the nodes from the DOM
  this.modal.addEventListener(this.transitionEnd, function() {
    _.modal.parentNode.removeChild(_.modal);
  });
  this.overlay.addEventListener(this.transitionEnd, function() {
    if (_.overlay.parentNode) _.overlay.parentNode.removeChild(_.overlay);
  });

  return true;

};

Modal.prototype.closeCancel = function() {

  // Store the value of this
  var _ = this;

  // Remove the open class name
  this.modal.className = this.modal.className.replace(" ml-open", "");
  this.overlay.className = this.overlay.className.replace(" ml-open", "");

  // Listen for CSS transitionEnd event and then remove the nodes from the DOM
  this.modal.addEventListener(this.transitionEnd, function() {
    _.modal.parentNode.removeChild(_.modal);
  });
  this.overlay.addEventListener(this.transitionEnd, function() {
    if (_.overlay.parentNode) _.overlay.parentNode.removeChild(_.overlay);
  });

  return false;

};

// Private Methods

// Utility to extend defaults with user options
function extendDefaults(source, properties) {
  var property;

  for (property in properties) {
    if(properties.hasOwnProperty(property)) {
      source[property] = properties[property];
    }
  }

  return source;
}

// Utility method to determine which transitionEnd event is supported
function transitionSelect() {
  var el = document.createElement("div");
  if (el.style.WebkitTransition) return "WebkitTransitionEnd";
  if (el.style.OTransition) return "OTransitionEnd";
  return "transitionend";
}

// Method to construct a modal using defined options
function buildOut() {

  var content, contentHolder, docFrag;

  /*
   * If content is an HTML string, append the HTML string.
   * If content is a domNode, append its content.
   */

  if (typeof this.options.content === "string") {
    content = this.options.content;
  } else {
    content = this.options.content.innerHTML;
  }

  // Create a DocumentFragment to build with
  docFrag = document.createDocumentFragment();

  // Create modal element
  this.modal = document.createElement("div");
  this.modal.className = "ml-modal " + this.options.className;
  this.modal.style.maxWidth = this.options.maxWidth + "px";
  this.modal.style.minWidth = this.options.minWidth + "px";

  // If header option is true, add a header div
      if(this.options.header === true){
          this.header = document.createElement("div");
          this.header.className = "ml-header";
          this.header.innerHTML = "";
    this.modal.appendChild(this.header);
      }

  // If closeButton option is true, add a close button
  if (this.options.closeButton === true) {
    this.closeButton = document.createElement("button");
    this.closeButton.className = "ml-close close-button";
    this.closeButton.innerHTML = "";
    this.modal.appendChild(this.closeButton);
  }

  // If overlay option is true, add an overlay
  if (this.options.overlay === true) {
    this.overlay = document.createElement("div");
    this.overlay.className = "ml-overlay " + this.options.className;
    docFrag.appendChild(this.overlay);
  }

  // If autoOpen option is true, open modal
  if (this.options.autoOpen === true) this.open();

  // Create content area and append to modal
  contentHolder = document.createElement("div");
  contentHolder.className = "ml-content";
  contentHolder.innerHTML = content;
  this.modal.appendChild(contentHolder);


  if (this.options.confirmDialog === true) {
      this.confirmDialog = document.createElement("div");
      this.confirmDialog.className = "ml-confirm-dialog";
      this.confirmDialog.innerHTML = "";
      //this.confirmDialog.innerHTML = "<button class='ml-dialog-btn ok btn'>OK</button><button class='ml-dialog-btn cancel btn'>Cancel</button>";
    this.modal.appendChild(this.confirmDialog);
  }

      // If headerIcon option is true, add icon to the header div
      if(this.options.headerIcon === true){
          this.headerIcon = document.createElement("div");
          this.headerIcon.className = "ml-header-icon";
          this.headerIcon.innerHTML = "";
    this.header.appendChild(this.headerIcon);
      }


      // If dontSaveBtn is true, a cancel btn is added to the confirm box
  if (this.options.dontSaveBtn === true) {
      this.dontSaveBtn = document.createElement("button");
      this.dontSaveBtn.className = "ml-dialog-btn no-save btn";
      this.dontSaveBtn.innerHTML = "Don't Save";
    this.confirmDialog.appendChild(this.dontSaveBtn);
  }

  // If cancelBtn is true, a confirm btn is added to the confirm box
  if (this.options.cancelBtn === true) {
      this.cancelBtn = document.createElement("button");
      this.cancelBtn.className = "ml-dialog-btn cancel btn";
      this.cancelBtn.innerHTML = "Cancel";
    this.confirmDialog.appendChild(this.cancelBtn);
  }

  // If saveBtn is true, a confirm btn is added to the confirm box
  if (this.options.saveBtn === true) {
      this.saveBtn = document.createElement("button");
      this.saveBtn.className = "ml-dialog-btn save btn";
      this.saveBtn.innerHTML = "Save";

          // event listener for the popup save btn
          this.saveBtn.addEventListener("click", popupSave, false);
      function popupSave() { save(); }

    this.confirmDialog.appendChild(this.saveBtn);
  }


  // Append modal to DocumentFragment
  docFrag.appendChild(this.modal);

  // Append DocumentFragment to body
  document.body.appendChild(docFrag);

}

function initializeEvents() {
  if (this.closeButton) {
    this.closeButton.addEventListener('click', this.close.bind(this));
  }

  if (this.overlay) {
    this.overlay.addEventListener('click', this.close.bind(this));
  }
  if (this.saveBtn) {
    this.saveBtn.addEventListener('click', this.closeConfirm.bind(this));
  }
  if (this.cancelBtn) {
    this.cancelBtn.addEventListener('click', this.closeConfirm.bind(this));
  }

  if (this.dontSaveBtn) {
    this.dontSaveBtn.addEventListener('click', this.closeCancel.bind(this));
  }

}
