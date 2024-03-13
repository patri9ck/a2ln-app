/*
 * Android 2 Linux Notifications - A way to display Android phone notifications on Linux
 * Copyright (C) 2023  patri9ck and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package dev.patri9ck.a2ln.log;

import android.view.LayoutInflater;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.patri9ck.a2ln.R;

public class LogDialogBuilder extends MaterialAlertDialogBuilder {

    public LogDialogBuilder(String log, LayoutInflater layoutInflater) {
        super(layoutInflater.getContext(), R.style.Dialog);

        setTitle(R.string.log_dialog_title);
        setMessage(log);
        setNegativeButton(R.string.cancel, null);
    }

    public LogDialogBuilder(KeptLog keptLog, LayoutInflater layoutInflater) {
        this(keptLog.format(), layoutInflater);
    }
}
