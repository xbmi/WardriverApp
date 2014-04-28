package com.inf8405.wardriver.db;

// Listener synchrone sur la base de donnees pour que les communications ne sont pas bloquantes
public interface DBSyncListener
{
	public void onDBSynced();
}
