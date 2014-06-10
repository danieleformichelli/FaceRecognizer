package com.eim.utilities;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
	List<Fragment> sections;

	public SectionsPagerAdapter(FragmentManager fm, List<Fragment> sections) {
		super(fm);

		this.sections = sections;
	}

	@Override
	public Fragment getItem(int position) {
		if (sections == null)
			throw new IllegalStateException(
					"Cannot call getItem if sections is null");

		if (sections.size() < position + 1)
			throw new IllegalStateException("Index out of bound: position "
					+ position + ", size " + sections.size());

		return sections.get(position);
	}

	@Override
	public int getCount() {
		if (sections == null)
			throw new IllegalStateException(
					"Cannot call getItem if sections is null");

		return sections.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {

		if (sections == null)
			throw new IllegalStateException(
					"Cannot call getItem if sections is null");

		return sections.get(position).toString();
	}
}
