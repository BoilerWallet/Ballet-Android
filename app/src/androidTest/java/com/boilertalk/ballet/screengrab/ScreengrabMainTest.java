package com.boilertalk.ballet.screengrab;


import android.support.annotation.CheckResult;
import android.support.test.espresso.AmbiguousViewMatcherException;
import android.support.test.espresso.FailureHandler;
import android.support.test.espresso.NoMatchingRootException;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.boilertalk.ballet.R;
import com.boilertalk.ballet.login.LoginActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsInstanceOf.any;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ScreengrabMainTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void createAccountTest() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editText_pass),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.editText_pass_container),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editText_pass),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.editText_pass_container),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("123shithole"), closeSoftKeyboard());

        try {
            ViewInteraction appCompatEditText3 = onView(
                    allOf(withId(R.id.editText_passconfirm),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.support.design.widget.TextInputLayout")),
                                            0),
                                    0),
                            isDisplayed()));
            appCompatEditText3.perform(replaceText("123shithole"), closeSoftKeyboard());
        } catch (Exception e) {

        }

        // Register Screen screenshot
        Screengrab.screenshot("register_screen");

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.button_go), withText("Go"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                0),
                        isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction imageButton = onView(
                allOf(withId(R.id.add_wallet_button),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_content_view),
                                        0),
                                1),
                        isDisplayed()));
        imageButton.check(matches(isDisplayed()));

        // Create all accounts
        createAccount("Grocery budget");
        createAccount("Kids account");
        createAccount("Totally awesome account");
        createAccount("\"Pharmaceutical\" incomes");
        createAccount("Trading account");

        // Wallet list screenshot
        Screengrab.screenshot("wallet_list_screen");

        ViewInteraction linearLayout2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.wallets_list_view),
                                0),
                        0),
                        isDisplayed()));
        linearLayout2.check(matches(isDisplayed()));

        // *** Receive screen

        ViewInteraction bottomNavigationItemView3 = onView(
                allOf(withId(R.id.navigation_receive),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation),
                                        0),
                                2),
                        isDisplayed()));
        bottomNavigationItemView3.perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.receive_select_account_button), withText("select account"),
                        childAtPosition(
                                allOf(withId(R.id.from_account_block),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                0)),
                                2),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.wallets_list_view),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                1)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        ViewInteraction textView = onView(
                allOf(withId(R.id.selected_account_text), withText("Grocery budget"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class),
                                        1),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Grocery budget")));
    }

    private void createAccount(String name) {
        // Create account

        ViewInteraction floatingActionButton = onView(
                allOf(withId(R.id.add_wallet_button),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.navigation_content_view),
                                        0),
                                1),
                        isDisplayed()));
        floatingActionButton.perform(click());

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.create_wallet_blockie_1),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                0)));
        linearLayout.perform(scrollTo(), click());

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.add_wallet_input_name),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.input_layout_password),
                                        0),
                                0)));
        appCompatEditText5.perform(scrollTo(), click());

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.add_wallet_input_name),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.input_layout_password),
                                        0),
                                0)));
        appCompatEditText6.perform(scrollTo(), replaceText(name), closeSoftKeyboard());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.add_wallet_input_name), withText(name),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.input_layout_password),
                                        0),
                                0)));
        appCompatEditText7.perform(pressImeActionButton());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.button_create), withText("Create"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        5),
                                0)));
        appCompatButton2.perform(scrollTo(), click());

        // Reload wallet list
        onView(withId(R.id.wallets_list_view)).perform(swipeDown());

        // End Create account
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
