/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdent;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.util.BinaryPredicate;
import org.benf.cfr.reader.util.MapFactory;

public class SSAIdentifiers<KEYTYPE> {
    private final KEYTYPE fixedHere;
    private final SSAIdent valFixedHere;
    private final Map<KEYTYPE, SSAIdent> knownIdentifiers;
    private boolean initialAssign = false;

    public SSAIdentifiers() {
        this.fixedHere = null;
        this.valFixedHere = null;
        this.knownIdentifiers = MapFactory.newMap();
    }

    public SSAIdentifiers(SSAIdentifiers<KEYTYPE> other) {
        this.fixedHere = other.fixedHere;
        this.valFixedHere = other.valFixedHere;
        this.knownIdentifiers = MapFactory.newMap();
        this.knownIdentifiers.putAll(other.knownIdentifiers);
        this.initialAssign = other.initialAssign;
    }

    public SSAIdentifiers(KEYTYPE lValue, SSAIdentifierFactory<KEYTYPE> ssaIdentifierFactory) {
        SSAIdent id = ssaIdentifierFactory.getIdent(lValue);
        this.fixedHere = lValue;
        this.valFixedHere = id;
        this.knownIdentifiers = MapFactory.newMap();
        this.knownIdentifiers.put(lValue, id);
    }

    public SSAIdentifiers(Map<KEYTYPE, SSAIdent> precomputedIdentifiers) {
        this.knownIdentifiers = precomputedIdentifiers;
        this.fixedHere = null;
        this.valFixedHere = null;
    }

    public void setInitialAssign() {
        this.initialAssign = true;
    }

    public boolean isInitialAssign() {
        return this.initialAssign;
    }

    public boolean mergeWith(SSAIdentifiers<KEYTYPE> other) {
        return this.mergeWith(other, null);
    }

    public boolean mergeWith(SSAIdentifiers<KEYTYPE> other, BinaryPredicate<KEYTYPE, KEYTYPE> pred) {
        boolean changed = false;
        for (Map.Entry<KEYTYPE, SSAIdent> valueSetEntry : other.knownIdentifiers.entrySet()) {
            KEYTYPE lValue = valueSetEntry.getKey();
            SSAIdent otherIdent = valueSetEntry.getValue();
            if (lValue.equals(this.fixedHere)) {
                if (pred == null) continue;
                if (!pred.test(lValue, this.fixedHere)) continue;
            }
            if (!this.knownIdentifiers.containsKey(lValue)) {
                this.knownIdentifiers.put(lValue, otherIdent);
                changed = true;
                continue;
            }
            SSAIdent oldIdent = this.knownIdentifiers.get(lValue);
            SSAIdent newIdent = oldIdent.mergeWith(otherIdent);
            if (newIdent.equals(oldIdent)) continue;
            this.knownIdentifiers.put(lValue, newIdent);
            changed = true;
        }
        return changed;
    }

    public boolean isFixedHere(KEYTYPE lValue) {
        return lValue.equals(this.fixedHere);
    }

    public KEYTYPE getFixedHere() {
        return this.fixedHere;
    }

    public SSAIdent getValFixedHere() {
        return this.valFixedHere;
    }

    public boolean isValidReplacement(LValue lValue, SSAIdentifiers<KEYTYPE> other) {
        SSAIdent thisVersion = this.knownIdentifiers.get(lValue);
        SSAIdent otherVersion = other.knownIdentifiers.get(lValue);
        if (thisVersion == null && otherVersion == null) {
            return true;
        }
        if (thisVersion != null && otherVersion != null) return thisVersion.equals(otherVersion);
        return false;
    }

    public SSAIdent getSSAIdent(KEYTYPE lValue) {
        return this.knownIdentifiers.get(lValue);
    }

    public int size() {
        return this.knownIdentifiers.size();
    }

    public Map<KEYTYPE, SSAIdent> getKnownIdentifiers() {
        return this.knownIdentifiers;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<KEYTYPE, SSAIdent> entry : this.knownIdentifiers.entrySet()) {
            sb.append(entry.getKey()).append("@").append(entry.getValue()).append(" ");
        }
        return sb.toString();
    }
}

