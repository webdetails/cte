// Create immediately invoked functional expression to wrap modal code
(function() {

	// Define constructor
	this.Modal = function() {

		// Create global element references
		this.closeButton = null;
		this.modal = null;
		this.overlay = null;

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
		}

		// Create options by extending defaults with the passed in arguments
		if (arguments[0] && typeof arguments[0] === "object") {
			this.options = extendDefaults(defaults, arguments[0]);
		}
		
	}

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
	}

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

	}

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
	}

}());