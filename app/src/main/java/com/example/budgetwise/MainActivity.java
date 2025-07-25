package com.example.budgetwise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.budgetwise.classes.FinancialAccount;
import com.example.budgetwise.classes.Limit;
import com.example.budgetwise.classes.Transaction;
import com.example.budgetwise.classes.TransactionType;
import com.example.budgetwise.firebase.FirebaseAuthService;
import com.example.budgetwise.fragments.AccountsFragment;
import com.example.budgetwise.fragments.Calculator503020Fragment;
import com.example.budgetwise.fragments.DashboardFragment;
import com.example.budgetwise.fragments.LimitFragment;
import com.example.budgetwise.fragments.ReportFragment;
import com.example.budgetwise.fragments.TransactionFragment;
import com.example.budgetwise.fragments.UserFragment;
import com.example.budgetwise.login.LoginActivity;
import com.example.budgetwise.viewmodel.AccountViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {
    public static final String TRANSACTION_TYPE = "transaction_type";
    public static final String RESULT_TYPE = "result_type";
    public static final String IS_UPDATE = "IS_UPDATE";
    public static final String UPDATE_TRANSACTION = "UPDATE_TRANSACTION";
    public static final String UPDATE_ACCOUNT = "UPDATE_ACCOUNT";
    public static final String IS_UPDATE_ACCOUNT = "IS_UPDATE_ACCOUNT";
    public static final String UPDATE_LIMIT = "UPDATE_LIMIT";
    public static final String IS_UPDATE_LIMIT = "IS_UPDATE_LIMIT";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Fragment currentFragment;
    private ActivityResultLauncher<Intent> launcher;
    private FloatingActionButton fabAddTransaction;
    private FloatingActionButton fabAddTransactionIncome;
    private FloatingActionButton fabAddTransactionExpense;
    private LinearLayout fabMenu;
    private AccountsFragment accountsFragment;
    private LimitFragment limitFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configNavigation();
        navigationView=findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(getItemSelectedMenuListener());

        launcher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),getAddTransactionsCallback());

        if(savedInstanceState==null){
            currentFragment=new TransactionFragment();
            getSupportActionBar().setTitle("Transactions");
            openFragment();
            navigationView.setCheckedItem(R.id.nav_transactions);
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_purple));
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("CRASH_HANDLER", "App crashed", throwable);
        });
    }

    public void setFab()
    {
        fabAddTransaction=findViewById(R.id.fabAddTransaction);
        fabAddTransactionIncome=findViewById(R.id.fab_income);
        fabAddTransactionExpense=findViewById(R.id.fab_expense);
        fabMenu=findViewById(R.id.fab_menu);

        fabAddTransaction.setVisibility(View.GONE);
        if(currentFragment instanceof TransactionFragment)
        {
            fabAddTransaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (fabMenu.getVisibility() == View.GONE) {
                        fabMenu.setVisibility(View.VISIBLE);
                        fabAddTransaction.setImageResource(R.drawable.close);
                    } else {
                        fabMenu.setVisibility(View.GONE);
                        fabAddTransaction.setImageResource(R.drawable.plus);
                    }
                }
            });

            fabAddTransactionExpense.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchAddTransaction(TransactionType.EXPENSE);
                }
            });

            fabAddTransactionIncome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchAddTransaction(TransactionType.INCOME);
                }
            });

        }else if(currentFragment instanceof AccountsFragment)
        {
            fabAddTransaction.setImageResource(R.drawable.plus);
            fabAddTransaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), AddAccountActivity.class);
                    launcher.launch(intent);
                }
            });
        }
    }

    public void setVisibilityFab(boolean isVisible)
    {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        if (fabAddTransaction != null)
        {
            fabAddTransaction.setVisibility(visibility);
            if(fabAddTransaction.getVisibility()==View.GONE)
            {
                fabMenu.setVisibility(visibility);
            }
            if(currentFragment instanceof AccountsFragment)
            {
                fabAddTransaction.setVisibility(View.VISIBLE);
                fabMenu.setVisibility(View.GONE);
            }

        }

    }

    private void launchAddTransaction(TransactionType type) {
        Intent intent = new Intent(getApplicationContext(), AddTransactionActivity.class);
        intent.putExtra(TRANSACTION_TYPE, type.name());
        launcher.launch(intent);
    }

    private void openFragment() {
        String tag = null;

        if (currentFragment instanceof AccountsFragment) {
            tag = "AccountsFragment";
        } else if (currentFragment instanceof Calculator503020Fragment) {
            tag = "Calculator503020Fragment";
        } else if (currentFragment instanceof DashboardFragment) {
            tag = "DashboardFragment";
        } else if (currentFragment instanceof LimitFragment) {
            tag = "LimitFragment";
        } else if (currentFragment instanceof ReportFragment) {
            tag = "ReportFragment";
        } else if (currentFragment instanceof TransactionFragment) {
            tag = "TransactionFragment";
        }else if (currentFragment instanceof UserFragment) {
            tag = "UserFragment";
        }
        getSupportFragmentManager()//Access the FragmentManager, i.e. the manager responsible for adding, replacing, and removing fragments from an activity
                .beginTransaction()//method available at the FragmentManager class level. Calling it specifies to the activity that a fragment is to be attached to it
                .replace(R.id.main_fl_container, currentFragment,tag)//method available at the FragmentManager class level. Calling it ensures that the component is replaced with the content of a fragment.
                .commit();//method available at the FragmentManager class level. Calling it ensures that the fragment is displayed on the mobile device screen

        setFab();
    }

    private ActivityResultCallback<ActivityResult> getAddTransactionsCallback() {
        return new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (currentFragment instanceof AccountsFragment) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String resultType = result.getData().getStringExtra(MainActivity.RESULT_TYPE);
                        if (AddAccountActivity.ADDED_ACCOUNT.equals(resultType)) {
                            Toast.makeText(MainActivity.this, R.string.account_added_successfully, Toast.LENGTH_SHORT).show();
                            if (currentFragment instanceof AccountsFragment) {
                                ((AccountsFragment) currentFragment).showAccounts();
                            }
                        }else if (AddAccountActivity.UPDATED_ACCOUNT.equals(resultType)) {
                            Toast.makeText(MainActivity.this, R.string.account_updated_successfully, Toast.LENGTH_SHORT).show();
                            ((AccountsFragment) currentFragment).showAccounts();
                        }
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Toast.makeText(MainActivity.this, R.string.account_addition_canceled, Toast.LENGTH_SHORT).show();
                    }
                } else if (currentFragment instanceof TransactionFragment) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String resultType = result.getData().getStringExtra(MainActivity.RESULT_TYPE);

                        if (AddTransactionActivity.ADDED_TRANSACTION.equals(resultType)) {
                            Toast.makeText(MainActivity.this, R.string.transaction_added_successfully, Toast.LENGTH_SHORT).show();
                            if (currentFragment instanceof TransactionFragment) {
                                ((TransactionFragment) currentFragment).reloadData();
                            }
                        } else if (AddTransactionActivity.UPDATED_TRANSACTION.equals(resultType)) {
                            Toast.makeText(MainActivity.this, R.string.transaction_updated_successfully, Toast.LENGTH_SHORT).show();
                            ((TransactionFragment) currentFragment).reloadData();
                        }
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Toast.makeText(MainActivity.this, R.string.transaction_addition_was_canceled, Toast.LENGTH_SHORT).show();
                    }
                }else if (currentFragment instanceof LimitFragment) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String resultType = result.getData().getStringExtra(MainActivity.RESULT_TYPE);

                        if (AddLimitActivity.ADDED_LIMIT.equals(resultType)) {
                            Toast.makeText(MainActivity.this, R.string.limit_added_successfully, Toast.LENGTH_SHORT).show();
                            if (currentFragment instanceof LimitFragment) {
                               ((LimitFragment) currentFragment).reloadData();
                            }
                        } else if (AddLimitActivity.UPDATED_LIMIT.equals(resultType)) {
                            Toast.makeText(MainActivity.this, R.string.limit_updated_successfully, Toast.LENGTH_SHORT).show();
                            ((LimitFragment) currentFragment).reloadData();
                        }
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Toast.makeText(MainActivity.this, R.string.adding_the_limit_has_been_canceled, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }


    public void openUpdateTransaction(Transaction transaction)
    {
        Intent intent=new Intent(this, AddTransactionActivity.class);
        intent.putExtra(UPDATE_TRANSACTION,transaction);
        intent.putExtra(IS_UPDATE,true);
        launcher.launch(intent);
    }

    public void openUpdateAccount(FinancialAccount account)
    {
        Intent intent=new Intent(this, AddAccountActivity.class);
        intent.putExtra(UPDATE_ACCOUNT,account);
        intent.putExtra(IS_UPDATE_ACCOUNT,true);
        launcher.launch(intent);
    }

    public void openUpdateLimit(Limit limit)
    {
        Intent intent=new Intent(this, AddLimitActivity.class);
        intent.putExtra(UPDATE_LIMIT,limit);
        intent.putExtra(IS_UPDATE_LIMIT,true);
        launcher.launch(intent);
    }

    private NavigationView.OnNavigationItemSelectedListener getItemSelectedMenuListener() {
        return item -> {
            if(item.getItemId()==R.id.nav_dashboard)
            {
                getSupportActionBar().setTitle("Dashboard");
                currentFragment=new DashboardFragment();
            }
            else if(item.getItemId()==R.id.nav_transactions)
            {
                getSupportActionBar().setTitle("Transactions");
                currentFragment=new TransactionFragment();
            } else if(item.getItemId()==R.id.nav_accounts)
            {
                getSupportActionBar().setTitle("Accounts");
                if (accountsFragment == null) {
                    accountsFragment = new AccountsFragment();
                }
                currentFragment = accountsFragment;
            }
            else if(item.getItemId()==R.id.nav_limits)
            {
                getSupportActionBar().setTitle("Goals");
                if (limitFragment == null) {
                    limitFragment = new LimitFragment();
                }
                currentFragment = limitFragment;
            }
            else if(item.getItemId()==R.id.nav_prediction)
            {
                getSupportActionBar().setTitle("Charts");
                currentFragment=new ReportFragment();
            }
            else if(item.getItemId()==R.id.nav_calculator)
            {
                getSupportActionBar().setTitle("50/30/20 calculator");
                currentFragment=new Calculator503020Fragment();
            }else if(item.getItemId()==R.id.nav_user)
            {
                getSupportActionBar().setTitle("User");
                currentFragment=new UserFragment();
            }
            drawerLayout.closeDrawers();//after selecting an item from the menu, the side menu closes
            openFragment();
            invalidateOptionsMenu();
            return true;
        };
    }
    private void configNavigation()
    {
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout=findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);//the burger icon synchronizes with the side menu and is displayed
        toggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        //show the "+" button only if the visible fragment is LimitFragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_fl_container);
        if(!(currentFragment instanceof LimitFragment))
        {
            menu.findItem(R.id.action_add_limit).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_limit) {
            Intent intent = new Intent(this, AddLimitActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences=getSharedPreferences("prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putLong("last_used",System.currentTimeMillis()).apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        long lastUsed = prefs.getLong("last_used", 0);
        long now = System.currentTimeMillis();
        long timeout = 5 * 60 * 1000; //30 min
        if (lastUsed != 0 && now - lastUsed > timeout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
