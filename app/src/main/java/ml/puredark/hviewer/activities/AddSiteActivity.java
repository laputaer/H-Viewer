package ml.puredark.hviewer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.github.clans.fab.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ml.puredark.hviewer.HViewerApplication;
import ml.puredark.hviewer.R;
import ml.puredark.hviewer.beans.Rule;
import ml.puredark.hviewer.beans.Selector;
import ml.puredark.hviewer.beans.Site;
import ml.puredark.hviewer.helpers.HViewerHttpClient;

import static java.util.regex.Pattern.DOTALL;

public class AddSiteActivity extends AnimationActivity {

    @BindView(R.id.view_add_site_json)
    View viewAddSiteJson;
    @BindView(R.id.view_site_details)
    View viewSiteDetails;
    @BindView(R.id.btn_return)
    ImageView btnReturn;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab_add)
    FloatingActionButton fabAdd;
    @BindView(R.id.shadowDown)
    View shadowDown;

    @BindView(R.id.input_site)
    MaterialEditText inputSite;
    @BindView(R.id.btn_parse_json)
    ButtonFlat btnParseJson;

    private SitePropViewHolder holder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_site);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        /* 为返回按钮加载图标 */
        setReturnButton(btnReturn);

        holder = new SitePropViewHolder(viewSiteDetails);

    }

    @OnClick(R.id.btn_return)
    void back() {
        onBackPressed();
    }

    @OnClick(R.id.btn_qr_scan)
    void scan() {
        IntentIntegrator integrator = new IntentIntegrator(AddSiteActivity.this);
        integrator.setCaptureActivity(MyCaptureActivity.class);
        integrator.setOrientationLocked(true);
        integrator.setPrompt("请扫描二维码");
        integrator.addExtra("SCAN_WIDTH", 480);
        integrator.addExtra("SCAN_HEIGHT", 480);
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }

    @OnClick(R.id.btn_parse_json)
    void parseJson() {
        String rule = inputSite.getText().toString();
        Site newSite = parseSite(rule);
        if (newSite == null)
            return;
        holder.fillSitePropEditText(newSite);
    }

    @OnClick(R.id.fab_add)
    void add() {
        Site newSite = holder.fromEditTextToSite();
        HViewerApplication.siteHolder.addSite(newSite);
        Intent intent = new Intent();
        intent.putExtra("sid", newSite.sid);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null && result.getContents() != null) {
            Log.d("AddSiteActivity", result.getContents());
            HViewerHttpClient.get(result.getContents(), new HViewerHttpClient.OnResponseListener() {
                @Override
                public void onSuccess(String result) {
                    Log.d("AddSiteActivity", result);
                    Pattern pattern = Pattern.compile("\\{[^<>]*\\}", Pattern.DOTALL);
                    Matcher matcher = pattern.matcher(result);
                    while(matcher.find()){
                        result = matcher.group();
                        Log.d("AddSiteActivity", result);
                        if(result.contains("indexRule"))
                            break;
                    }
                    Site newSite = parseSite(result);
                    if (newSite == null)
                        return;
                    holder.fillSitePropEditText(newSite);
                }

                @Override
                public void onFailure(HViewerHttpClient.HttpError error) {
                    Toast.makeText(AddSiteActivity.this, error.getErrorString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Site parseSite(String json) {
        try {
            Site site = new Gson().fromJson(json, Site.class);
            int sid = HViewerApplication.siteHolder.getSites().size() + 1;
            site.sid = sid;
            if (site.indexRule == null || site.galleryRule == null)
                Toast.makeText(this, "输入的规则缺少信息", Toast.LENGTH_SHORT).show();
            return site;
        } catch (JsonSyntaxException e) {
            Toast.makeText(this, "输入规则格式错误", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public class SitePropViewHolder {
        @BindView(R.id.input_title)
        MaterialEditText inputTitle;
        @BindView(R.id.input_indexUrl)
        MaterialEditText inputIndexUrl;
        @BindView(R.id.input_galleryUrl)
        MaterialEditText inputGalleryUrl;
        @BindView(R.id.input_searchUrl)
        MaterialEditText inputSearchUrl;
        @BindView(R.id.input_picUrlSelector_selector)
        MaterialEditText inputPicUrlSekectorSelector;
        @BindView(R.id.input_picUrlSelector_regex)
        MaterialEditText inputPicUrlSekectorRegex;
        @BindView(R.id.input_picUrlSelector_replacement)
        MaterialEditText inputPicUrlSekectorReplacement;
        @BindView(R.id.input_indexRule_item_selector)
        MaterialEditText inputIndexRuleItemSelector;
        @BindView(R.id.input_indexRule_item_regex)
        MaterialEditText inputIndexRuleItemRegex;
        @BindView(R.id.input_indexRule_item_replacement)
        MaterialEditText inputIndexRuleItemReplacement;
        @BindView(R.id.input_indexRule_idCode_selector)
        MaterialEditText inputIndexRuleIdCodeSelector;
        @BindView(R.id.input_indexRule_idCode_regex)
        MaterialEditText inputIndexRuleIdCodeRegex;
        @BindView(R.id.input_indexRule_idCode_replacement)
        MaterialEditText inputIndexRuleIdCodeReplacement;
        @BindView(R.id.input_indexRule_title_selector)
        MaterialEditText inputIndexRuleTitleSelector;
        @BindView(R.id.input_indexRule_title_regex)
        MaterialEditText inputIndexRuleTitleRegex;
        @BindView(R.id.input_indexRule_title_replacement)
        MaterialEditText inputIndexRuleTitleReplacement;
        @BindView(R.id.input_indexRule_uploader_selector)
        MaterialEditText inputIndexRuleUploaderSelector;
        @BindView(R.id.input_indexRule_uploader_regex)
        MaterialEditText inputIndexRuleUploaderRegex;
        @BindView(R.id.input_indexRule_uploader_replacement)
        MaterialEditText inputIndexRuleUploaderReplacement;
        @BindView(R.id.input_indexRule_cover_selector)
        MaterialEditText inputIndexRuleCoverSelector;
        @BindView(R.id.input_indexRule_cover_regex)
        MaterialEditText inputIndexRuleCoverRegex;
        @BindView(R.id.input_indexRule_cover_replacement)
        MaterialEditText inputIndexRuleCoverReplacement;
        @BindView(R.id.input_indexRule_category_selector)
        MaterialEditText inputIndexRuleCategorySelector;
        @BindView(R.id.input_indexRule_category_regex)
        MaterialEditText inputIndexRuleCategoryRegex;
        @BindView(R.id.input_indexRule_category_replacement)
        MaterialEditText inputIndexRuleCategoryReplacement;
        @BindView(R.id.input_indexRule_datetime_selector)
        MaterialEditText inputIndexRuleDatetimeSelector;
        @BindView(R.id.input_indexRule_datetime_regex)
        MaterialEditText inputIndexRuleDatetimeRegex;
        @BindView(R.id.input_indexRule_datetime_replacement)
        MaterialEditText inputIndexRuleDatetimeReplacement;
        @BindView(R.id.input_indexRule_rating_selector)
        MaterialEditText inputIndexRuleRatingSelector;
        @BindView(R.id.input_indexRule_rating_regex)
        MaterialEditText inputIndexRuleRatingRegex;
        @BindView(R.id.input_indexRule_rating_replacement)
        MaterialEditText inputIndexRuleRatingReplacement;
        @BindView(R.id.input_indexRule_tags_selector)
        MaterialEditText inputIndexRuleTagsSelector;
        @BindView(R.id.input_indexRule_tags_regex)
        MaterialEditText inputIndexRuleTagsRegex;
        @BindView(R.id.input_indexRule_tags_replacement)
        MaterialEditText inputIndexRuleTagsReplacement;
        @BindView(R.id.input_indexRule_pictureUrl_selector)
        MaterialEditText inputIndexRulePictureUrlSelector;
        @BindView(R.id.input_indexRule_pictureUrl_regex)
        MaterialEditText inputIndexRulePictureUrlRegex;
        @BindView(R.id.input_indexRule_pictureUrl_replacement)
        MaterialEditText inputIndexRulePictureUrlReplacement;
        @BindView(R.id.input_indexRule_pictureThumbnail_selector)
        MaterialEditText inputIndexRulePictureThumbnailSelector;
        @BindView(R.id.input_indexRule_pictureThumbnail_regex)
        MaterialEditText inputIndexRulePictureThumbnailRegex;
        @BindView(R.id.input_indexRule_pictureThumbnail_replacement)
        MaterialEditText inputIndexRulePictureThumbnailReplacement;
        @BindView(R.id.input_galleryRule_item_selector)
        MaterialEditText inputGalleryRuleItemSelector;
        @BindView(R.id.input_galleryRule_item_regex)
        MaterialEditText inputGalleryRuleItemRegex;
        @BindView(R.id.input_galleryRule_item_replacement)
        MaterialEditText inputGalleryRuleItemReplacement;
        @BindView(R.id.input_galleryRule_idCode_selector)
        MaterialEditText inputGalleryRuleIdCodeSelector;
        @BindView(R.id.input_galleryRule_idCode_regex)
        MaterialEditText inputGalleryRuleIdCodeRegex;
        @BindView(R.id.input_galleryRule_idCode_replacement)
        MaterialEditText inputGalleryRuleIdCodeReplacement;
        @BindView(R.id.input_galleryRule_title_selector)
        MaterialEditText inputGalleryRuleTitleSelector;
        @BindView(R.id.input_galleryRule_title_regex)
        MaterialEditText inputGalleryRuleTitleRegex;
        @BindView(R.id.input_galleryRule_title_replacement)
        MaterialEditText inputGalleryRuleTitleReplacement;
        @BindView(R.id.input_galleryRule_uploader_selector)
        MaterialEditText inputGalleryRuleUploaderSelector;
        @BindView(R.id.input_galleryRule_uploader_regex)
        MaterialEditText inputGalleryRuleUploaderRegex;
        @BindView(R.id.input_galleryRule_uploader_replacement)
        MaterialEditText inputGalleryRuleUploaderReplacement;
        @BindView(R.id.input_galleryRule_cover_selector)
        MaterialEditText inputGalleryRuleCoverSelector;
        @BindView(R.id.input_galleryRule_cover_regex)
        MaterialEditText inputGalleryRuleCoverRegex;
        @BindView(R.id.input_galleryRule_cover_replacement)
        MaterialEditText inputGalleryRuleCoverReplacement;
        @BindView(R.id.input_galleryRule_category_selector)
        MaterialEditText inputGalleryRuleCategorySelector;
        @BindView(R.id.input_galleryRule_category_regex)
        MaterialEditText inputGalleryRuleCategoryRegex;
        @BindView(R.id.input_galleryRule_category_replacement)
        MaterialEditText inputGalleryRuleCategoryReplacement;
        @BindView(R.id.input_galleryRule_datetime_selector)
        MaterialEditText inputGalleryRuleDatetimeSelector;
        @BindView(R.id.input_galleryRule_datetime_regex)
        MaterialEditText inputGalleryRuleDatetimeRegex;
        @BindView(R.id.input_galleryRule_datetime_replacement)
        MaterialEditText inputGalleryRuleDatetimeReplacement;
        @BindView(R.id.input_galleryRule_rating_selector)
        MaterialEditText inputGalleryRuleRatingSelector;
        @BindView(R.id.input_galleryRule_rating_regex)
        MaterialEditText inputGalleryRuleRatingRegex;
        @BindView(R.id.input_galleryRule_rating_replacement)
        MaterialEditText inputGalleryRuleRatingReplacement;
        @BindView(R.id.input_galleryRule_tags_selector)
        MaterialEditText inputGalleryRuleTagsSelector;
        @BindView(R.id.input_galleryRule_tags_regex)
        MaterialEditText inputGalleryRuleTagsRegex;
        @BindView(R.id.input_galleryRule_tags_replacement)
        MaterialEditText inputGalleryRuleTagsReplacement;
        @BindView(R.id.input_galleryRule_pictureUrl_selector)
        MaterialEditText inputGalleryRulePictureUrlSelector;
        @BindView(R.id.input_galleryRule_pictureUrl_regex)
        MaterialEditText inputGalleryRulePictureUrlRegex;
        @BindView(R.id.input_galleryRule_pictureUrl_replacement)
        MaterialEditText inputGalleryRulePictureUrlReplacement;
        @BindView(R.id.input_galleryRule_pictureThumbnail_selector)
        MaterialEditText inputGalleryRulePictureThumbnailSelector;
        @BindView(R.id.input_galleryRule_pictureThumbnail_regex)
        MaterialEditText inputGalleryRulePictureThumbnailRegex;
        @BindView(R.id.input_galleryRule_pictureThumbnail_replacement)
        MaterialEditText inputGalleryRulePictureThumbnailReplacement;

        public SitePropViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        private String joinSelector(Selector selector) {
            String select = (selector.selector != null) ? "$(\"" + selector.selector + "\")" : "";
            String function = (selector.fun != null) ? "." + selector.fun : "";
            String parameter = (selector.param != null) ? "(\"" + selector.param + "\")" : "";
            String join = select + function + parameter;
            return join;
        }

        private Selector splitSelector(Selector selector) {
            Pattern pattern = Pattern.compile("\\$\\(\"(.*?)\"\\).?(\\w*)?\\(?\"?(\\w*)\"?\\)?", DOTALL);
            Matcher matcher = pattern.matcher(selector.selector);
            if (matcher.find() && matcher.groupCount() >= 3) {
                selector.selector = matcher.group(1);
                selector.fun = matcher.group(2);
                selector.param = matcher.group(3);
            }
            return selector;
        }

        private void fillSitePropEditText(Site site) {
            inputTitle.setText(site.title);
            inputIndexUrl.setText(site.indexUrl);
            inputGalleryUrl.setText(site.galleryUrl);
            inputSearchUrl.setText(site.searchUrl);
            if (site.picUrlSelector != null) {
                inputPicUrlSekectorSelector.setText(joinSelector(site.picUrlSelector));
                inputPicUrlSekectorRegex.setText(site.picUrlSelector.regex);
                inputPicUrlSekectorReplacement.setText(site.picUrlSelector.replacement);
            }
            if (site.indexRule != null) {
                if (site.indexRule.item != null) {
                    inputIndexRuleItemSelector.setText(joinSelector(site.indexRule.item));
                    inputIndexRuleItemRegex.setText(site.indexRule.item.regex);
                    inputIndexRuleItemReplacement.setText(site.indexRule.item.replacement);
                }
                if (site.indexRule.idCode != null) {
                    inputIndexRuleIdCodeSelector.setText(joinSelector(site.indexRule.idCode));
                    inputIndexRuleIdCodeRegex.setText(site.indexRule.idCode.regex);
                    inputIndexRuleIdCodeReplacement.setText(site.indexRule.idCode.replacement);
                }
                if (site.indexRule.title != null) {
                    inputIndexRuleTitleSelector.setText(joinSelector(site.indexRule.title));
                    inputIndexRuleTitleRegex.setText(site.indexRule.title.regex);
                    inputIndexRuleTitleReplacement.setText(site.indexRule.title.replacement);
                }
                if (site.indexRule.uploader != null) {
                    inputIndexRuleUploaderSelector.setText(joinSelector(site.indexRule.uploader));
                    inputIndexRuleUploaderRegex.setText(site.indexRule.uploader.regex);
                    inputIndexRuleUploaderReplacement.setText(site.indexRule.uploader.replacement);
                }
                if (site.indexRule.cover != null) {
                    inputIndexRuleCoverSelector.setText(joinSelector(site.indexRule.cover));
                    inputIndexRuleCoverRegex.setText(site.indexRule.cover.regex);
                    inputIndexRuleCoverReplacement.setText(site.indexRule.cover.replacement);
                }
                if (site.indexRule.category != null) {
                    inputIndexRuleCategorySelector.setText(joinSelector(site.indexRule.category));
                    inputIndexRuleCategoryRegex.setText(site.indexRule.category.regex);
                    inputIndexRuleCategoryReplacement.setText(site.indexRule.category.replacement);
                }
                if (site.indexRule.datetime != null) {
                    inputIndexRuleDatetimeSelector.setText(joinSelector(site.indexRule.datetime));
                    inputIndexRuleDatetimeRegex.setText(site.indexRule.datetime.regex);
                    inputIndexRuleDatetimeReplacement.setText(site.indexRule.datetime.replacement);
                }
                if (site.indexRule.rating != null) {
                    inputIndexRuleRatingSelector.setText(joinSelector(site.indexRule.rating));
                    inputIndexRuleRatingRegex.setText(site.indexRule.rating.regex);
                    inputIndexRuleRatingReplacement.setText(site.indexRule.rating.replacement);
                }
                if (site.indexRule.tags != null) {
                    inputIndexRuleTagsSelector.setText(joinSelector(site.indexRule.tags));
                    inputIndexRuleTagsRegex.setText(site.indexRule.tags.regex);
                    inputIndexRuleTagsReplacement.setText(site.indexRule.tags.replacement);
                }
                if (site.indexRule.pictureUrl != null) {
                    inputIndexRulePictureUrlSelector.setText(joinSelector(site.indexRule.pictureUrl));
                    inputIndexRulePictureUrlRegex.setText(site.indexRule.pictureUrl.regex);
                    inputIndexRulePictureUrlReplacement.setText(site.indexRule.pictureUrl.replacement);
                }
                if (site.indexRule.pictureThumbnail != null) {
                    inputIndexRulePictureThumbnailSelector.setText(joinSelector(site.indexRule.pictureThumbnail));
                    inputIndexRulePictureThumbnailRegex.setText(site.indexRule.pictureThumbnail.regex);
                    inputIndexRulePictureThumbnailReplacement.setText(site.indexRule.pictureThumbnail.replacement);
                }
            }
            if (site.galleryRule != null) {
                if (site.galleryRule.item != null) {
                    inputGalleryRuleItemSelector.setText(joinSelector(site.galleryRule.item));
                    inputGalleryRuleItemRegex.setText(site.galleryRule.item.regex);
                    inputGalleryRuleItemReplacement.setText(site.galleryRule.item.replacement);
                }
                if (site.galleryRule.idCode != null) {
                    inputGalleryRuleIdCodeSelector.setText(joinSelector(site.galleryRule.idCode));
                    inputGalleryRuleIdCodeRegex.setText(site.galleryRule.idCode.regex);
                    inputGalleryRuleIdCodeReplacement.setText(site.galleryRule.idCode.replacement);
                }
                if (site.galleryRule.title != null) {
                    inputGalleryRuleTitleSelector.setText(joinSelector(site.galleryRule.title));
                    inputGalleryRuleTitleRegex.setText(site.galleryRule.title.regex);
                    inputGalleryRuleTitleReplacement.setText(site.galleryRule.title.replacement);
                }
                if (site.galleryRule.uploader != null) {
                    inputGalleryRuleUploaderSelector.setText(joinSelector(site.galleryRule.uploader));
                    inputGalleryRuleUploaderRegex.setText(site.galleryRule.uploader.regex);
                    inputGalleryRuleUploaderReplacement.setText(site.galleryRule.uploader.replacement);
                }
                if (site.galleryRule.cover != null) {
                    inputGalleryRuleCoverSelector.setText(joinSelector(site.galleryRule.cover));
                    inputGalleryRuleCoverRegex.setText(site.galleryRule.cover.regex);
                    inputGalleryRuleCoverReplacement.setText(site.galleryRule.cover.replacement);
                }
                if (site.galleryRule.category != null) {
                    inputGalleryRuleCategorySelector.setText(joinSelector(site.galleryRule.category));
                    inputGalleryRuleCategoryRegex.setText(site.galleryRule.category.regex);
                    inputGalleryRuleCategoryReplacement.setText(site.galleryRule.category.replacement);
                }
                if (site.galleryRule.datetime != null) {
                    inputGalleryRuleDatetimeSelector.setText(joinSelector(site.galleryRule.datetime));
                    inputGalleryRuleDatetimeRegex.setText(site.galleryRule.datetime.regex);
                    inputGalleryRuleDatetimeReplacement.setText(site.galleryRule.datetime.replacement);
                }
                if (site.galleryRule.rating != null) {
                    inputGalleryRuleRatingSelector.setText(joinSelector(site.galleryRule.rating));
                    inputGalleryRuleRatingRegex.setText(site.galleryRule.rating.regex);
                    inputGalleryRuleRatingReplacement.setText(site.galleryRule.rating.replacement);
                }
                if (site.galleryRule.tags != null) {
                    inputGalleryRuleTagsSelector.setText(joinSelector(site.galleryRule.tags));
                    inputGalleryRuleTagsRegex.setText(site.galleryRule.tags.regex);
                    inputGalleryRuleTagsReplacement.setText(site.galleryRule.tags.replacement);
                }
                if (site.galleryRule.pictureUrl != null) {
                    inputGalleryRulePictureUrlSelector.setText(joinSelector(site.galleryRule.pictureUrl));
                    inputGalleryRulePictureUrlRegex.setText(site.galleryRule.pictureUrl.regex);
                    inputGalleryRulePictureUrlReplacement.setText(site.galleryRule.pictureUrl.replacement);
                }
                if (site.galleryRule.pictureThumbnail != null) {
                    inputGalleryRulePictureThumbnailSelector.setText(joinSelector(site.galleryRule.pictureThumbnail));
                    inputGalleryRulePictureThumbnailRegex.setText(site.galleryRule.pictureThumbnail.regex);
                    inputGalleryRulePictureThumbnailReplacement.setText(site.galleryRule.pictureThumbnail.replacement);
                }
            }
        }

        private Site fromEditTextToSite() {
            Site site = new Site();
            site.title = loadString(inputTitle);
            site.indexUrl = loadString(inputIndexUrl);
            site.galleryUrl = loadString(inputGalleryUrl);
            site.searchUrl = loadString(inputSearchUrl);
            site.picUrlSelector = loadSelector(inputPicUrlSekectorSelector, inputPicUrlSekectorRegex, inputPicUrlSekectorReplacement);

            //index rule
            site.indexRule = new Rule();
            site.indexRule.item = loadSelector(inputIndexRuleItemSelector, inputIndexRuleItemRegex, inputIndexRuleItemReplacement);
            site.indexRule.idCode = loadSelector(inputIndexRuleIdCodeSelector, inputIndexRuleIdCodeRegex, inputIndexRuleIdCodeReplacement);
            site.indexRule.title = loadSelector(inputIndexRuleTitleSelector, inputIndexRuleTitleRegex, inputIndexRuleTitleReplacement);
            site.indexRule.uploader = loadSelector(inputIndexRuleUploaderSelector, inputIndexRuleUploaderRegex, inputIndexRuleUploaderReplacement);
            site.indexRule.cover = loadSelector(inputIndexRuleCoverSelector, inputIndexRuleCoverRegex, inputIndexRuleCoverReplacement);
            site.indexRule.category = loadSelector(inputIndexRuleCategorySelector, inputIndexRuleCategoryRegex, inputIndexRuleCategoryReplacement);
            site.indexRule.datetime = loadSelector(inputIndexRuleDatetimeSelector, inputIndexRuleDatetimeRegex, inputIndexRuleDatetimeReplacement);
            site.indexRule.rating = loadSelector(inputIndexRuleRatingSelector, inputIndexRuleRatingRegex, inputIndexRuleRatingReplacement);
            site.indexRule.tags = loadSelector(inputIndexRuleTagsSelector, inputIndexRuleTagsRegex, inputIndexRuleTagsReplacement);
            site.indexRule.pictureUrl = loadSelector(inputIndexRulePictureUrlSelector, inputIndexRulePictureUrlRegex, inputIndexRulePictureUrlReplacement);
            site.indexRule.pictureThumbnail = loadSelector(inputIndexRulePictureThumbnailSelector, inputIndexRulePictureThumbnailRegex, inputIndexRulePictureThumbnailReplacement);

            //gallery rule
            site.galleryRule = new Rule();
            site.galleryRule.item = loadSelector(inputGalleryRuleItemSelector, inputGalleryRuleItemRegex, inputGalleryRuleItemReplacement);
            site.galleryRule.idCode = loadSelector(inputGalleryRuleIdCodeSelector, inputGalleryRuleIdCodeRegex, inputGalleryRuleIdCodeReplacement);
            site.galleryRule.title = loadSelector(inputGalleryRuleTitleSelector, inputGalleryRuleTitleRegex, inputGalleryRuleTitleReplacement);
            site.galleryRule.uploader = loadSelector(inputGalleryRuleUploaderSelector, inputGalleryRuleUploaderRegex, inputGalleryRuleUploaderReplacement);
            site.galleryRule.cover = loadSelector(inputGalleryRuleCoverSelector, inputGalleryRuleCoverRegex, inputGalleryRuleCoverReplacement);
            site.galleryRule.category = loadSelector(inputGalleryRuleCategorySelector, inputGalleryRuleCategoryRegex, inputGalleryRuleCategoryReplacement);
            site.galleryRule.datetime = loadSelector(inputGalleryRuleDatetimeSelector, inputGalleryRuleDatetimeRegex, inputGalleryRuleDatetimeReplacement);
            site.galleryRule.rating = loadSelector(inputGalleryRuleRatingSelector, inputGalleryRuleRatingRegex, inputGalleryRuleRatingReplacement);
            site.galleryRule.tags = loadSelector(inputGalleryRuleTagsSelector, inputGalleryRuleTagsRegex, inputGalleryRuleTagsReplacement);
            site.galleryRule.pictureUrl = loadSelector(inputGalleryRulePictureUrlSelector, inputGalleryRulePictureUrlRegex, inputGalleryRulePictureUrlReplacement);
            site.galleryRule.pictureThumbnail = loadSelector(inputGalleryRulePictureThumbnailSelector, inputGalleryRulePictureThumbnailRegex, inputGalleryRulePictureThumbnailReplacement);

            return site;
        }

        private String loadString(EditText editText) {
            String text = editText.getText().toString();
            return ("".equals(text)) ? null : text;
        }

        private Selector loadSelector(EditText inputSelector, EditText inputRegex, EditText inputReplace) {
            Selector selector = new Selector();
            String sel = inputSelector.getText().toString();
            if (!"".equals(sel)) {
                selector.selector = sel;
                selector = splitSelector(selector);
            }
            String regex = inputRegex.getText().toString();
            if (!"".equals(regex)) {
                selector.regex = regex;
            }
            String replace = inputReplace.getText().toString();
            if (!"".equals(replace)) {
                selector.replacement = replace;
            }
            return (selector.selector == null) ? null : selector;
        }

    }
}
