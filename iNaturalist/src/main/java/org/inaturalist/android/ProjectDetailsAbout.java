package org.inaturalist.android;

import com.evernote.android.state.State;

import com.livefront.bridge.Bridge;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

public class ProjectDetailsAbout extends AppCompatActivity {
    public static final String KEY_PROJECT = "project";
    @State(AndroidStateBundlers.BetterJSONObjectBundler.class) public BetterJSONObject mProject;
    private INaturalistApp mApp;

    @Override
	protected void onStop()
	{
		super.onStop();

	}
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bridge.restoreInstanceState(this, savedInstanceState);


        mApp = (INaturalistApp) getApplicationContext();
        mApp.applyLocaleSettings(getBaseContext());

        setContentView(R.layout.project_details_about);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setLogo(R.drawable.ic_arrow_back);
        actionBar.setTitle(R.string.about_project);

        TextView title = (TextView) findViewById(R.id.project_title);
        TextView projectDescription = (TextView) findViewById(R.id.project_description);
        TextView projectTerms = (TextView) findViewById(R.id.project_terms);
        TextView projectRules = (TextView) findViewById(R.id.project_rules);
        ViewGroup projectTermsContainer = (ViewGroup) findViewById(R.id.terms_container);
        ViewGroup projectRulesContainer = (ViewGroup) findViewById(R.id.rules_container);

        final Intent intent = getIntent();
        if (savedInstanceState == null) {
            mProject = (BetterJSONObject) intent.getSerializableExtra(KEY_PROJECT);
        }

        title.setText(mProject.getString("title"));
        String description = mProject.getString("description");
        HtmlUtils.fromHtml(projectDescription, description);

        String terms = mProject.getString("terms");
        if ((terms != null) && (terms.length() > 0)) {
            projectTermsContainer.setVisibility(View.VISIBLE);
            HtmlUtils.fromHtml(projectTerms, terms);
        } else {
            projectTermsContainer.setVisibility(View.GONE);
        }

        String rules = mProject.getString("project_observation_rule_terms");
        if ((rules != null) && (rules.length() > 0)) {
            projectRulesContainer.setVisibility(View.VISIBLE);
            String[] rulesSplit = rules.split("\\|");
            String rulesFinal = StringUtils.join(rulesSplit, "<br/>&#8226; ");
            rulesFinal = "&#8226; " + rulesFinal;
            HtmlUtils.fromHtml(projectRules, rulesFinal);
        } else {
            projectRulesContainer.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bridge.saveInstanceState(this, outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
