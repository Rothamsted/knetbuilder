package net.sourceforge.ondex.core.persistent;

import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.base.EvidenceTypeImpl;
import net.sourceforge.ondex.core.util.UpdateListener;

public class BerkeleyEvidenceType extends EvidenceTypeImpl implements
		Updatable, BerkeleySerializable {
	public static final Persistence.MetaDataFactory<BerkeleyEvidenceType, EvidenceType> FACTORY = new Persistence.MetaDataFactory<BerkeleyEvidenceType, EvidenceType>(
			"[EvidenceType - deserialize]") {
		@Override
		public BerkeleyEvidenceType create(long sid, String id,
				String fullname, String description) {
			return new BerkeleyEvidenceType(sid, id, fullname, description);
		}

		@Override
		BerkeleyEvidenceType convert(EvidenceType old) {
			return new BerkeleyEvidenceType(old.getSID(), old.getId(),
					old.getFullname(), old.getDescription());
		}
	};

	private UpdateListener l;

	public BerkeleyEvidenceType(long sid, String id, String fullname,
			String description) {
		super(sid, id, fullname, description);
	}

	@Override
	public byte[] serialise() {
		return serialise(getSID());
	}

	@Override
	public byte[] serialise(long sid) {
		return SerialiseMetaDataFactory.serialise(sid, this);
	}

	@Override
	public void fireUpdateEvent() {
		if (l != null)
			l.performUpdate(this);
	}

	@Override
	public void setUpdateListener(UpdateListener l) {
		this.l = l;
	}

	@Override
	public void setFullname(String s) {
		super.setFullname(s);
		fireUpdateEvent();
	}

	@Override
	public void setDescription(String s) {
		super.setDescription(s);
		fireUpdateEvent();
	}

}
