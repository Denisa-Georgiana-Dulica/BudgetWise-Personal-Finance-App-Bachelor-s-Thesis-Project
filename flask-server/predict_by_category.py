import firebase_admin
from firebase_admin import credentials,db
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib
matplotlib.use('Agg')
from datetime import datetime
import io
import base64


#I initialize Firebase with the JSON key and database link
try:
    firebase_admin.get_app()
except ValueError:
    cred = credentials.Certificate("budget-wise-83703-firebase-adminsdk-fbsvc-cdca31f9d2.json")
    firebase_admin.initialize_app(cred, {
        'databaseURL': 'https://budget-wise-83703-default-rtdb.firebaseio.com/'
    })


def extract_transactions():
    try:
        # Încarcă categoriile și tipul 503020 pentru fiecare
        categories_data = db.reference("categories").get()
        category_types = {}
        for category_id, info in categories_data.items():
            name = info.get("categoryName")
            ctype = info.get("categoryType503020")
            if name and ctype:
                category_types[name] = ctype

        transactions_data = db.reference("transactions").get()
        if not transactions_data:
            return pd.DataFrame()

        records = []

        for user_id, transactions in transactions_data.items():
            if not isinstance(transactions, dict):
                continue

            for transaction_id, t in transactions.items():
                if not isinstance(t, dict):
                    continue

                amount = t.get("transactionAmount")
                category = t.get("transactionCategory", {}).get("categoryName")
                typeT = t.get("transactionType")

                include = False
                if typeT == "EXPENSE":
                    include = True
                elif typeT == "INCOME" and category in category_types and category_types[category] == "SAVING":
                    include = True

                if not include or not amount or not category:
                    continue

                try:
                    amount = float(amount)
                except (ValueError, TypeError):
                    continue

                success_dates_set = set()

                if "successDates" in t and isinstance(t["successDates"], list):
                    for success in t["successDates"]:
                        if isinstance(success, dict) and "time" in success:
                            try:
                                dt = datetime.fromtimestamp(success["time"] / 1000)
                                success_dates_set.add(dt)
                            except:
                                continue

                if "transactionDate" in t and isinstance(t["transactionDate"], dict) and "time" in t["transactionDate"]:
                    try:
                        base_date = datetime.fromtimestamp(t["transactionDate"]["time"] / 1000)
                        if not success_dates_set or base_date not in success_dates_set:
                            records.append({
                                "date": base_date,
                                "amount": amount,
                                "category": category
                            })
                    except:
                        pass

                for dt in success_dates_set:
                    records.append({
                        "date": dt,
                        "amount": amount,
                        "category": category
                    })

        df = pd.DataFrame(records)
        return df

    except Exception as e:
        print(f"Error extracting transactions: {e}")
        return pd.DataFrame()

def generate_bar_chart(data):
    categories = list(data.keys())
    amounts = list(data.values())
    
    fig, ax = plt.subplots(figsize=(20, 18))
    bars = ax.bar(categories, amounts, color="#5a9fef", width=0.6) 
    
    for bar in bars:
        height = bar.get_height()
        ax.annotate(f'{height:.0f}',
                    xy=(bar.get_x() + bar.get_width() / 2, height),
                    xytext=(0, 15),
                    textcoords="offset points",
                    ha='center', va='bottom',
                    fontsize=40, weight='bold')
    
    ax.set_title("Current month expenses", fontsize=55, weight='bold', pad=30)
    ax.set_xlabel("Category", fontsize=45, labelpad=20,weight='bold')
    ax.set_ylabel("Amount", fontsize=45, labelpad=20,weight='bold')
    ax.tick_params(axis='x', labelsize=45)
    ax.tick_params(axis='y', labelsize=45)
    
    if amounts:
        ax.set_ylim(0, max(amounts) * 1.2)
    
    ax.grid(True, alpha=0.3, axis='y')
    ax.set_axisbelow(True)
    plt.tight_layout()
    
    buffer = io.BytesIO()
    plt.savefig(buffer, format="png", dpi=150, bbox_inches='tight')
    buffer.seek(0)
    plt.close(fig)

    base64_image = base64.b64encode(buffer.read()).decode('utf-8')
    return base64_image

def generate_line_chart(labels, values):
    if not labels or not values:
        return None
    fig,ax=plt.subplots(figsize=(11, 10))
    ax.plot(labels, values, color="#5a9fef", marker='o', linewidth=2)  # culoare consistentă
    ax.set_title("Expenses trend (last 6 months)", fontsize=35, weight='bold', pad=30)
    ax.set_xlabel("Month", fontsize=25, labelpad=20,weight='bold')
    ax.set_ylabel("Amount", fontsize=25, labelpad=20,weight='bold')
    ax.tick_params(axis='x', labelsize=24)
    ax.tick_params(axis='y', labelsize=24)
    if values:
        y_margin = (max(values) - min(values)) * 0.1 if len(values) > 1 else max(values) * 0.1
        ax.set_ylim(min(values) - y_margin, max(values) + y_margin)
    ax.grid(True, alpha=0.3)
    ax.set_axisbelow(True)
    plt.tight_layout()
    buf = io.BytesIO()
    plt.savefig(buf, format='png', dpi=100, bbox_inches='tight')
    buf.seek(0)
    plt.close(fig)
    return base64.b64encode(buf.read()).decode('utf-8')

#line chart for each category with data from the last 6 months
def generate_category_line_chart(category_name):
    df=extract_transactions()
    if df.empty:
        return None
    df["year_month"]=df["date"].dt.to_period("M").astype(str) #Period-String
    df=df[df["category"]==category_name]

    if df.empty:
        return None

    monthly=(df.groupby("year_month")["amount"].sum().reset_index().sort_values("year_month"))

    #last 6 months
    last=monthly.tail(6)
    labels=last["year_month"].tolist()
    values=last["amount"].round(2).tolist()
    if len(labels)==0:
        return None
    
    chart = generate_line_chart(labels, values)

    return chart

def generate_reports():    
    df=extract_transactions()
    if df.empty:
        return {"error":"No data"}
    df["year_month"]=df["date"].dt.to_period("M").astype(str)
    current_month = datetime.now().strftime("%Y-%m")
    df = df[df["year_month"] == current_month]
    if df.empty:
        return {"error":"No data current month"}
    bar_data = (
            df.groupby("category")["amount"]
            .sum()
            .sort_values(ascending=False)
            .to_dict()
        )
    bar_chart= generate_bar_chart(bar_data)
    if not bar_chart:
        return {"error": "Failed to generate chart"}
    
    result = {"bar_chart": bar_chart}
    return result

def generate_503020(need_limit, want_limit, saving_limit):
    df = extract_transactions()
    if df.empty:
        return None

    try:
        categories = db.reference("categories").get()
        category_types = {}
        for categoryId, info in categories.items():
            name = info.get("categoryName")
            ctype = info.get("categoryType503020")
            if name and ctype:
                category_types[name] = ctype
    except Exception as e:
        print(f"Error loading category types: {e}")
        return None

    df["type503020"] = df["category"].map(category_types)
    df = df[df["type503020"].isin(["NEED", "WANT", "SAVING"])]

    df["year_month"] = df["date"].dt.to_period("M").astype(str)
    current_month = datetime.now().strftime("%Y-%m")
    df = df[df["year_month"] == current_month]
    if df.empty:
        return {"error": "No data current month"}

    groups = df.groupby("type503020")["amount"].sum().to_dict()
    data = {
        "NEED": groups.get("NEED", 0),
        "WANT": groups.get("WANT", 0),
        "SAVING": groups.get("SAVING", 0)
    }

    limits = {
        "NEED": need_limit,
        "WANT": want_limit,
        "SAVING": saving_limit,
    }

    fig, ax = plt.subplots(figsize=(16, 12))
    colors = []

    for key in ["NEED", "WANT", "SAVING"]:
        val = data.get(key, 0)
        limit = limits.get(key, 0)

        if key in ["NEED", "WANT"]:
            colors.append("red" if val > limit else "steelblue")
        elif key == "SAVING":
            colors.append("green" if val >= limit else "steelblue")

    bars = ax.bar(data.keys(), data.values(), color=colors, width=0.6)
    for bar in bars:
        height = bar.get_height()
        ax.annotate(f'{height:.0f}',
                    xy=(bar.get_x() + bar.get_width() / 2, height),
                    xytext=(0, 45),
                    textcoords="offset points",
                    ha='center', va='bottom',
                    fontsize=28, weight='bold')

    ax.set_title("Evolution in the current month", fontsize=50, pad=15, weight='bold')
    ax.set_ylabel("Amount", fontsize=40, weight='bold')
    ax.set_xlabel("Category", fontsize=40, weight='bold')
    ax.tick_params(axis='x', labelsize=32)
    ax.tick_params(axis='y', labelsize=32)
    ax.set_ylim(0, max(data.values()) * 1.3 if data else 10)
    ax.grid(True, axis='y', alpha=0.3)
    ax.set_axisbelow(True)
    plt.tight_layout()

    buf = io.BytesIO()
    plt.savefig(buf, format='png', dpi=120)
    buf.seek(0)
    plt.close(fig)
    return base64.b64encode(buf.read()).decode("utf-8")
