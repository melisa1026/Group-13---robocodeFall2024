/*
 * Copyright (c) 2001-2023 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package net.sf.robocode.ui.editor;


import javax.swing.event.UndoableEditEvent;

import java.lang.reflect.Field;

import javax.swing.event.DocumentEvent.EventType;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;


/**
 * Undo manager that compounds undo and redo edits.
 * 
 * @author Flemming N. Larsen (original)
 */
@SuppressWarnings("serial")
public class CompoundUndoManager extends UndoManagerWithActions {

	private CompoundEdit currentCompoundEdit;
	private transient EventType lastEventType;  // Made lastEventType as transient
	private boolean isCompoundMarkStart;

	public CompoundUndoManager() {
		super();
		reset();
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent undoableEditEvent) {
		UndoableEdit edit = undoableEditEvent.getEdit();
		DefaultDocumentEvent event = getDocumentEvent(edit);

		if (event != null) {
			handleDocumentEvent(event, edit);
		}

		// Update the state of the actions
		updateUndoRedoState();
	}

	private DefaultDocumentEvent getDocumentEvent(UndoableEdit edit) {
		// If it's a DefaultDocumentEvent, return it directly
		if (edit instanceof DefaultDocumentEvent) {
			return (DefaultDocumentEvent) edit;
		}

		// No longer using reflection; handle the case when we can't retrieve the event directly
		return null; // Return null or handle this case appropriately if needed
	}

	private void handleDocumentEvent(DefaultDocumentEvent event, UndoableEdit edit) {
		EventType eventType = event.getType();

		if (eventType != EventType.CHANGE) {
			boolean isEndCompoundEdit = shouldEndCompoundEdit(eventType, event);

			if (!isCompoundMarkStart) {
				if (isEndCompoundEdit || eventType != lastEventType) {
					endCurrentCompoundEdit();
				}
				lastEventType = eventType;
			}

			if (currentCompoundEdit == null) {
				newCurrentCompoundEdit();
			}
		}

		if (currentCompoundEdit != null) {
			currentCompoundEdit.addEdit(edit);
		}
	}

	private boolean shouldEndCompoundEdit(EventType eventType, DefaultDocumentEvent event) {
		if (eventType == EventType.INSERT) {
			return containsNewLine(event);
		}
		return false;
	}

	private boolean containsNewLine(DefaultDocumentEvent event) {
		try {
			String insertedText = event.getDocument().getText(event.getOffset(), event.getLength());
			return insertedText.contains("\n");
		} catch (BadLocationException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void discardAllEdits() {
		super.discardAllEdits();
		reset();
	}

	/**
	 * Ends the current compound edit, and mark the start for combining the next insertions or removals of text to be
	 * put into the same compound edit so that these combined operations acts like as one single edit.
	 *
	 * @see #markCompoundEnd()
	 */
	public void markCompoundStart() {
		endCurrentCompoundEdit();
		isCompoundMarkStart = true;
	}

	/**
	 * Ends the current compound edit so that previous edits acts like a single edit.
	 * 
	 * @see #markCompoundStart()
	 */
	public void markCompoundEnd() {
		endCurrentCompoundEdit();
		isCompoundMarkStart = false;
	}

	private void reset() {
		currentCompoundEdit = null;
		lastEventType = EventType.INSERT; // important
	}

	private void endCurrentCompoundEdit() {
		if (currentCompoundEdit != null) {
			currentCompoundEdit.end();
			currentCompoundEdit = null;
		}
	}

	private void newCurrentCompoundEdit() {
		// Set current compound edit to a new one
		currentCompoundEdit = new CompoundEdit() {
			// Make sure canUndo() and canRedo() works
			@Override
			public boolean isInProgress() {
				return false;
			}

			@Override
			public void undo() throws CannotUndoException {
				endCurrentCompoundEdit();
				super.undo();
			}
		};
		// Add the current compound edit to the internal edits
		addEdit(currentCompoundEdit);
	}
}
